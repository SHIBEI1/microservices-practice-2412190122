#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
python3 -u - <<'PY'
import json
import os
import re
import subprocess
import sys
from datetime import datetime, time, timedelta, timezone
from urllib.error import HTTPError
from urllib.request import Request, urlopen
from uuid import uuid4
from zoneinfo import ZoneInfo

sys.path.insert(0, "scripts")
from test_auth import compose_config, gateway_url, login

DOCKER = [os.environ.get("DOCKER_BIN", "docker"), "compose"]
PET_SERVICES = ("pet-service", "pet-service-replica")


def compose(*args, capture=False):
    return subprocess.run([*DOCKER, *args], check=True, text=True,
                          stdout=subprocess.PIPE if capture else None).stdout


def wait_instances(service, count):
    subprocess.run([sys.executable, "scripts/nacos-admin.py", "wait", "--service",
                    service, "--count", str(count)], check=True)


def request(expected, url, token, body=None):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Authorization": "Bearer " + token}
    if data is not None:
        headers["Content-Type"] = "application/json"
    message = Request(url, data=data, headers=headers)
    try:
        response = urlopen(message, timeout=20)
    except HTTPError as error:
        response = error
    with response:
        status = response.code
        payload = json.load(response)
    print(f"{message.get_method()} {url} -> HTTP {status}")
    if status != expected:
        raise RuntimeError(f"预期 HTTP {expected}，实际 HTTP {status}，响应 {payload}")
    return payload


def verify():
    for service, count in [("pet-service", 2), ("doctor-service", 1), ("appointment-service", 1)]:
        wait_instances(service, count)

    base = gateway_url(compose_config())
    token = login(base, "admin")
    pet, doctor, appointment = [f"{base}/api/{path}" for path in ("pets", "doctors", "appointments")]

    def call(expected, url, body=None):
        return request(expected, url, token, body)

    run_id = uuid4().hex[:16]
    pet_id = call(201, pet, {
        "name": f"e2e-{run_id}", "species": "猫", "age": 2,
        "ownerName": "测试主人", "ownerPhone": "13800000000",
    })["id"]
    doctor_id = call(201, doctor, {
        "code": f"E2E-{run_id}", "name": "测试医生", "department": "内科",
        "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "17:00", "accepting": True,
    })["id"]
    today = datetime.now(ZoneInfo("Asia/Shanghai")).date()
    monday = today + timedelta(days=7 - today.weekday())
    first_slot = datetime.combine(monday, time(9))

    def booking(slot, participant=pet_id):
        return {"petId": participant, "doctorId": doctor_id,
                "appointmentTime": (first_slot + timedelta(minutes=30 * slot)).isoformat(),
                "reason": f"第八周端到端验证 {run_id}"}

    def appointment_ids():
        return {item["id"] for item in call(200, f"{appointment}?petId={pet_id}")}

    def create_and_read(body):
        created = call(201, appointment, body)
        found = call(200, f"{appointment}/{created['id']}")
        expected = {**body, "id": created["id"], "status": "PENDING"}
        for result in (created, found):
            if any(result[key] != value for key, value in expected.items()):
                raise RuntimeError(f"预约创建或查询结果不一致：{result}")
        return created["id"]

    since = datetime.now(timezone.utc).isoformat()
    expected_ids = {create_and_read(booking(slot)) for slot in range(6)}
    for service in PET_SERVICES:
        logs = compose("logs", "--no-color", "--since", since, service, capture=True)
        matches = re.findall(rf"宠物档案查询 petId={pet_id}\b", logs)
        if not matches:
            raise RuntimeError(f"{service} 没有处理本次宠物 {pet_id} 的查询")
        print(f"PASS {service} 宠物 {pet_id} 查询次数={len(matches)}")

    missing = call(404, appointment, booking(6, 9223372036854775807))
    if missing["detail"] != "宠物不存在" or appointment_ids() != expected_ids:
        raise RuntimeError("资源不存在时的错误响应或预约数据不正确")

    pending = booking(6)
    try:
        compose("stop", *PET_SERVICES)
        wait_instances("pet-service", 0)
        failure = call(503, appointment, pending)
        if "宠物" not in failure["detail"] or appointment_ids() != expected_ids:
            raise RuntimeError("依赖下线时的错误响应或预约数据不正确")
        print("PASS 宠物服务不可用，预约未写入")
    finally:
        compose("start", "--wait", "--wait-timeout", "120", *PET_SERVICES)

    wait_instances("pet-service", 2)
    expected_ids.add(create_and_read(pending))
    if appointment_ids() != expected_ids:
        raise RuntimeError("恢复后预约数据不正确")
    print("PASS 两个实例已恢复，原请求创建成功")
    print("PASS 端到端验证通过")


if __name__ == "__main__":
    try:
        verify()
    except (RuntimeError, ValueError, KeyError, OSError, subprocess.SubprocessError) as error:
        sys.exit(f"FAIL {error}")
PY
