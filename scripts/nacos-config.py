"""Publish only the checked-in, non-sensitive service configuration examples."""
import argparse
import importlib.util
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent
spec = importlib.util.spec_from_file_location('nacos_admin', ROOT / 'scripts/nacos-admin.py')
admin = importlib.util.module_from_spec(spec)
spec.loader.exec_module(admin)
SERVICES = ('appointment-service', 'user-service')
ENVIRONMENTS = ('dev', 'test', 'prod')
ALLOWED_KEYS = {'hospital.config.environment', 'hospital.config.revision', 'hospital.booking.enabled',
                'hospital.auth.expires-in-seconds', 'spring.datasource.hikari.maximum-pool-size',
                'logging.level.com.zjgsu.kl'}


def config_settings():
    config = admin.settings()
    config['namespace'] = config['namespace'] or 'public'
    import test_auth
    services = test_auth.compose_config()
    config['config_group'] = services['appointment-service']['environment'].get(
        'NACOS_CONFIG_GROUP', 'PET_HOSPITAL_CONFIG')
    return config


def coordinates(config, service, environment):
    return {'namespaceId': config['namespace'], 'groupName': config['config_group'],
            'dataId': f'{service}-{environment}.properties'}


def get(config, token, service, environment):
    result = admin.request(config, '/nacos/v3/admin/cs/config', token=token,
                           query=coordinates(config, service, environment))
    return result['content']


def publish(config, token, service, environment, content):
    keys = {line.partition('=')[0].strip() for line in content.splitlines()
            if line.strip() and not line.lstrip().startswith('#')}
    if not keys <= ALLOWED_KEYS:
        raise RuntimeError('配置包含不允许发布的键')
    if admin.request(config, '/nacos/v3/admin/cs/config', token=token,
                     data={**coordinates(config, service, environment), 'content': content,
                           'type': 'properties', 'appName': service}) is not True:
        raise RuntimeError('Nacos 配置发布失败')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--env', choices=ENVIRONMENTS, required=True)
    parser.add_argument('--overwrite', action='store_true', help='显式以仓库示例覆盖已有配置')
    args = parser.parse_args()
    config = config_settings()
    token = admin.login(config)
    for service in SERVICES:
        content = (ROOT / 'configs/nacos' / args.env / f'{service}-{args.env}.properties').read_text()
        try:
            existing = get(config, token, service, args.env)
        except RuntimeError as error:
            if 'HTTP 404' not in str(error):
                raise
            existing = None
        if existing is not None and not args.overwrite:
            print(f'SKIP {service}-{args.env}.properties 已存在；保留当前配置')
            continue
        publish(config, token, service, args.env, content)
        if get(config, token, service, args.env) != content:
            raise RuntimeError('配置发布后读取不一致')
        print(f'PASS 发布 {service}-{args.env}.properties Group={config["config_group"]}')


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, OSError, ValueError, KeyError, subprocess.SubprocessError) as error:
        sys.exit(f'FAIL {error}')
