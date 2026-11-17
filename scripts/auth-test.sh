#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
python3 -u - <<'PY'
import os
import secrets
import subprocess
import sys
from uuid import uuid4

sys.path.insert(0, "scripts")
from test_auth import compose_config, gateway_url, login, request


def expect(label, expected, url, **kwargs):
    status, payload = request(url, **kwargs)
    if status != expected or (status in (401, 403) and not isinstance(payload, dict)):
        raise RuntimeError(f"{label}，预期 HTTP {expected}，实际 HTTP {status}")
    print(f"PASS {label} -> HTTP {status}")
    return payload


def verify():
    base = gateway_url(compose_config())
    health = expect("公开健康检查", 200, base + "/actuator/health")
    if health.get("status") != "UP":
        raise RuntimeError("网关健康状态异常")

    pets = base + "/api/pets"
    doctors = base + "/api/doctors"
    appointments = base + "/api/appointments"
    spoofed = {"X-User-Id": "admin", "X-Username": "admin", "X-Role": "ADMIN",
               "X-Roles": "ADMIN", "X-User-Role": "ADMIN", "X-User-Roles": "ADMIN"}

    expect("未登录访问宠物接口", 401, pets)
    expect("仅携带伪造身份请求头", 401, pets, headers=spoofed)
    expect("错误密码登录", 401, base + "/api/auth/login",
           body={"username": "owner", "password": secrets.token_urlsafe(24)})

    owner = login(base, "owner")
    print("PASS 普通用户登录并取得 Bearer JWT -> HTTP 200")
    admin = login(base, "admin")
    print("PASS 管理员登录并取得 Bearer JWT -> HTTP 200")

    for label, url in (("宠物", pets), ("医生", doctors), ("预约", appointments)):
        records = expect(f"携带有效 JWT 查询{label}", 200, url, token=owner)
        if not isinstance(records, list):
            raise RuntimeError(f"{label}查询未返回业务列表")

    header, claims, signature = owner.split(".")
    altered = ("A" if signature[0] != "A" else "B") + signature[1:]
    expect("签名校验失败", 401, pets, token=f"{header}.{claims}.{altered}")

    doctor = {"code": "AUTH-" + uuid4().hex[:16].upper(), "name": "权限验证医生", "department": "内科",
              "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00", "accepting": True}
    expect("普通用户访问医生管理接口", 403, doctors, token=owner, body=doctor)
    expect("普通用户伪造管理员请求头", 403, doctors, token=owner, headers=spoofed, body=doctor)
    created = expect("管理员登记医生", 201, doctors, token=admin, body=doctor)
    if created.get("code") != doctor["code"] or not created.get("id"):
        raise RuntimeError("未返回正确的医生记录")

    docker = os.environ.get("DOCKER_BIN", "docker")
    for service in ("pet-service", "pet-service-replica", "doctor-service",
                    "appointment-service", "user-service"):
        containers = subprocess.run([docker, "compose", "ps", "-q", service],
                                    check=True, capture_output=True, text=True).stdout.split()
        if not containers:
            raise RuntimeError(f"{service} 未运行")
        for container in containers:
            published = subprocess.run([docker, "port", container],
                                       check=True, capture_output=True, text=True).stdout.strip()
            if published:
                raise RuntimeError(f"{service} 存在宿主机端口映射")
        print(f"PASS {service} 未发布宿主机端口")


try:
    verify()
except (RuntimeError, OSError, ValueError, KeyError, subprocess.SubprocessError) as error:
    sys.exit(f"FAIL {error}")

print("PASS 网关认证与授权验证通过")
PY
