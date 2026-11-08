from pathlib import Path
import argparse
import json
import os
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


SERVICES = ("pet-service", "doctor-service", "appointment-service")


def settings():
    root = Path(__file__).resolve().parent.parent
    candidates = [os.environ["DOCKER_BIN"]] if os.environ.get("DOCKER_BIN") else ["docker", "docker.exe"]
    for command in candidates:
        try:
            probe = subprocess.run([command, "compose", "version"], capture_output=True, timeout=10)
        except (OSError, subprocess.TimeoutExpired):
            continue
        if probe.returncode == 0:
            break
    else:
        raise RuntimeError("Docker Compose 不可用")
    result = subprocess.run(
        [command, "compose", "config", "--format", "json"],
        cwd=root, capture_output=True, text=True, timeout=30,
    )
    if result.returncode != 0:
        raise RuntimeError("Compose 配置解析失败，请检查本地 .env")
    services = json.loads(result.stdout)["services"]
    ports = services["nacos"]["ports"]
    port = next(item["published"] for item in ports if int(item["target"]) == 8848)
    env = services["pet-service"]["environment"]
    password = env.get("NACOS_PASSWORD", "")
    if not password:
        raise RuntimeError("请在本地 .env 设置 NACOS_PASSWORD")
    return {
        "url": f"http://127.0.0.1:{port}",
        "username": env["NACOS_USERNAME"],
        "password": password,
        "namespace": env.get("NACOS_NAMESPACE") or "",
        "group": env["NACOS_GROUP"],
    }


def request(config, path, data=None, token=None, query=None):
    url = config["url"] + path
    if query:
        url += "?" + urlencode(query)
    headers = {"accessToken": token} if token else {}
    body = urlencode(data).encode() if data is not None else None
    message = Request(url, data=body, headers=headers)
    try:
        with urlopen(message, timeout=5) as response:
            payload = json.load(response)
    except HTTPError as error:
        raise RuntimeError(f"Nacos 接口返回 HTTP {error.code}") from None
    except (URLError, TimeoutError, ValueError):
        raise RuntimeError("Nacos 接口连接失败或响应无效") from None
    if payload.get("code", 0) not in (0, 200):
        raise RuntimeError("Nacos 接口调用失败，请检查初始化、凭据和命名空间")
    return payload.get("data", payload)


def login(config):
    result = request(config, "/nacos/v3/auth/user/login", data={
        "username": config["username"], "password": config["password"],
    })
    token = result.get("accessToken")
    if not token:
        raise RuntimeError("Nacos 登录失败")
    return token


def instances(config, token, service, healthy_only=False):
    return request(config, "/nacos/v3/client/ns/instance/list", token=token, query={
        "namespaceId": config["namespace"],
        "groupName": config["group"],
        "serviceName": service,
        "healthyOnly": str(healthy_only).lower(),
    })


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=("init", "list", "wait"))
    parser.add_argument("--service", choices=SERVICES, default="pet-service")
    parser.add_argument("--state", choices=("present", "absent"), default="present")
    parser.add_argument("--timeout", type=int, default=120)
    args = parser.parse_args()
    config = settings()
    if args.action == "init":
        try:
            login(config)
            print("Nacos 管理员已初始化，当前凭据登录成功。")
            return
        except RuntimeError:
            if config["username"] != "nacos":
                raise RuntimeError("首次初始化须使用 nacos 管理员")
        request(config, "/nacos/v3/auth/user/admin", data={"password": config["password"]})
        login(config)
        print("Nacos 管理员初始化成功，凭据保存在本地 .env。")
        return

    token = login(config)
    if args.action == "list":
        print(f"Namespace={config['namespace'] or 'public'} Group={config['group']}")
        for service in SERVICES:
            hosts = instances(config, token, service)
            print(f"{service} 实例数={len(hosts)}")
            for host in hosts:
                print(f"  {host['ip']}:{host['port']} healthy={host['healthy']} enabled={host['enabled']}")
        return

    deadline = time.monotonic() + args.timeout
    while time.monotonic() < deadline:
        hosts = instances(config, token, args.service, healthy_only=True)
        if bool(hosts) == (args.state == "present"):
            print(f"PASS {args.service} 可用实例数={len(hosts)}，预期状态={args.state}")
            return
        time.sleep(1)
    raise RuntimeError(f"{args.service} 未在 {args.timeout} 秒内达到 {args.state} 状态")


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, KeyError, ValueError, subprocess.SubprocessError) as error:
        print(f"ERROR {error}", file=sys.stderr)
        sys.exit(1)
