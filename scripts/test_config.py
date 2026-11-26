"""Verify actual Nacos consumption, refresh, business behavior and profile isolation."""
import fcntl
import importlib.util
import json
import os
import signal
from pathlib import Path
import subprocess
import sys
import tempfile
import time
from uuid import uuid4
from test_auth import compose_config, gateway_url, login, password_for, request

ROOT = Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location('nacos_config', ROOT / 'scripts/nacos-config.py')
nacos = importlib.util.module_from_spec(spec)
spec.loader.exec_module(nacos)
DOCKER = os.environ.get('DOCKER_BIN', 'docker')


def docker(*args, input=None):
    return subprocess.run([DOCKER, *args], cwd=ROOT, input=input, capture_output=True,
                          text=True, check=True, timeout=180).stdout.strip()


def wait(check, label):
    deadline = time.monotonic() + 90
    while time.monotonic() < deadline:
        try:
            result = check()
            if result:
                print(f'PASS {label}')
                return result
        except (RuntimeError, OSError, ValueError, KeyError, subprocess.SubprocessError):
            pass
        time.sleep(1)
    raise RuntimeError(f'{label}，等待超时')


def updated(content, **changes):
    values = dict(line.split('=', 1) for line in content.splitlines()
                  if line.strip() and not line.lstrip().startswith('#'))
    values.update(changes)
    return ''.join(f'{key}={value}\n' for key, value in values.items())


def verify():
    services = compose_config()
    environment = services['appointment-service']['environment'].get('APP_ENV', 'dev')
    if environment != 'dev':
        raise RuntimeError('验证脚本仅针对 dev 环境运行，避免修改 test 或 prod')
    base = gateway_url(services)
    admin_token = login(base, 'admin')
    owner_token = login(base, 'owner')
    config = nacos.config_settings()
    access = nacos.admin.login(config)
    before_status, before = request(base + '/api/appointments/config', token=admin_token)
    if before_status != 200 or before.get('environment') != 'dev':
        raise RuntimeError('预约服务未读取 dev 配置或诊断接口不可用')
    if request(base + '/api/appointments/config', token=owner_token)[0] != 403:
        raise RuntimeError('配置诊断接口未限制管理员')
    snapshot = nacos.get(config, access, 'appointment-service', 'dev')
    test_snapshot = nacos.get(config, access, 'user-service', 'test')
    probe = 'pet-hospital-config-test-' + uuid4().hex[:10]
    probe_created = False
    changed = False
    pending = None
    print('PASS 管理员读取非敏感配置，普通用户返回 HTTP 403')
    try:
        revision = 'verify-' + uuid4().hex[:10]
        changed = True
        nacos.publish(config, access, 'appointment-service', 'dev', updated(snapshot,
                      **{'hospital.booking.enabled': 'false', 'hospital.config.revision': revision}))
        def closed():
            status, payload = request(base + '/api/appointments/config', token=admin_token)
            return payload if status == 200 and payload.get('revision') == revision and payload.get('bookingEnabled') is False else None
        observed = wait(closed, 'Nacos 变更已被预约服务读取：bookingEnabled=false')
        if observed['startedAt'] != before['startedAt']:
            raise RuntimeError('配置刷新期间服务发生重启')
        _, records = request(base + '/api/appointments', token=admin_token)
        body = {'petId': 9223372036854775806, 'doctorId': 9223372036854775806,
                'appointmentTime': '2030-01-07T09:00:00', 'reason': '配置开关验证'}
        status, payload = request(base + '/api/appointments', token=admin_token, body=body)
        _, after = request(base + '/api/appointments', token=admin_token)
        if status != 503 or '预约登记暂时关闭' not in payload.get('detail', '') or records != after:
            raise RuntimeError('关闭预约后未正确拒绝业务或数据发生变化')
        print('PASS 关闭预约返回 HTTP 503，预约记录未增加，进程启动时间不变')
        nacos.publish(config, access, 'appointment-service', 'dev', updated(snapshot,
                      **{'hospital.booking.enabled': 'true', 'hospital.config.revision': revision + '-open'}))
        wait(lambda: request(base + '/api/appointments/config', token=admin_token)[1].get('bookingEnabled') is True,
             '恢复开关已被服务读取')
        if request(base + '/api/appointments', token=admin_token, body=body)[0] != 404:
            raise RuntimeError('恢复后未进入参与方校验')
        print('PASS 恢复后请求进入业务校验，缺失宠物返回 HTTP 404')
        # Same image and credentials, isolated test Data ID, no registration or published port.
        probe_created = True
        docker('compose', 'run', '-d', '--no-deps', '--name', probe, '-e', 'APP_ENV=test',
               '-e', 'SPRING_CLOUD_NACOS_DISCOVERY_REGISTER_ENABLED=false', 'user-service')
        def probe_login():
            text = docker('exec', '-i', probe, 'curl', '-fsS', '-H', 'Content-Type: application/json',
                          '--data-binary', '@-', 'http://127.0.0.1:8080/api/auth/login',
                          input=json.dumps({'username': 'owner', 'password': password_for('owner')}))
            return json.loads(text)
        expected = int(dict(line.split('=', 1) for line in test_snapshot.splitlines()
                            if line.strip() and not line.startswith('#'))['hospital.auth.expires-in-seconds'])
        wait(lambda: probe_login().get('expiresIn') == expected, f'test 实例从独立 Data ID 读取 JWT 有效期 {expected} 秒')
        status, payload = request(base + '/api/auth/login', body={'username': 'owner', 'password': password_for('owner')})
        dev_ttl = payload['expiresIn']
        if status != 200 or dev_ttl == expected:
            raise RuntimeError('dev/test 配置未区分')
        print(f'PASS dev JWT={dev_ttl} 秒，test JWT={expected} 秒，同一镜像加载不同环境配置')
        if docker('port', probe):
            raise RuntimeError('临时 test 实例不应发布端口')
        final = request(base + '/api/appointments/config', token=admin_token)[1]
        if final['startedAt'] != before['startedAt']:
            raise RuntimeError('动态验证发生服务重启')
    except BaseException as error:
        pending = error
    finally:
        errors = []
        if probe_created:
            try:
                docker('rm', '-f', probe)
            except subprocess.SubprocessError:
                errors.append('临时 test 实例清理失败')
        if changed:
            try:
                nacos.publish(config, access, 'appointment-service', 'dev', snapshot)
                expected_values = dict(line.split('=', 1) for line in snapshot.splitlines()
                                       if line.strip() and not line.startswith('#'))
                wait(lambda: request(base + '/api/appointments/config', token=admin_token)[1].get('revision') == expected_values['hospital.config.revision']
                     and request(base + '/api/appointments/config', token=admin_token)[1].get('bookingEnabled') == (expected_values['hospital.booking.enabled'] == 'true'),
                     '原始配置已恢复并被服务读取')
            except (RuntimeError, OSError, ValueError):
                errors.append('原始配置恢复失败，请检查 Nacos')
        if errors:
            raise RuntimeError('；'.join(errors)) from pending
    if pending:
        raise pending
    print('PASS 配置中心、动态业务开关和 dev/test 隔离验证通过')


if __name__ == '__main__':
    signal.signal(signal.SIGTERM, lambda *_: (_ for _ in ()).throw(KeyboardInterrupt()))
    try:
        with open(Path(tempfile.gettempdir()) / 'pet-hospital-config-test.lock', 'w') as lock:
            fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            verify()
    except (RuntimeError, OSError, ValueError, KeyError, subprocess.SubprocessError) as error:
        # Never print subprocess stdout/stderr: login responses contain live tokens.
        sys.exit('FAIL ' + (str(error) if not isinstance(error, subprocess.SubprocessError) else '容器命令执行失败'))
