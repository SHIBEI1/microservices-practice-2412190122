import json
import os
from pathlib import Path
import subprocess
from urllib.error import HTTPError
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent.parent


def compose_config():
    result = subprocess.run(
        [os.environ.get("DOCKER_BIN", "docker"), "compose", "config", "--format", "json"],
        cwd=ROOT, check=True, text=True, stdout=subprocess.PIPE,
    )
    return json.loads(result.stdout)["services"]


def gateway_url(services):
    port = services["gateway-service"]["ports"][0]["published"]
    return f"http://127.0.0.1:{port}"


def password_for(account):
    key = {"owner": "AUTH_OWNER_PASSWORD", "admin": "AUTH_ADMIN_PASSWORD"}[account]
    value = os.environ.get(key)
    if value:
        return value
    local = ROOT / ".env.auth-test"
    if local.is_file():
        for line in local.read_text().splitlines():
            name, _, value = line.partition("=")
            if name == key and value:
                return value
    raise RuntimeError(f"请为本地测试设置 {key}")


def request(url, body=None, token=None, headers=None):
    headers = dict(headers or {})
    if token:
        headers["Authorization"] = "Bearer " + token
    data = None
    if body is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(body).encode()
    try:
        response = urlopen(Request(url, data=data, headers=headers), timeout=15)
    except HTTPError as error:
        response = error
    with response:
        payload = json.load(response)
        return response.status, payload


def login(base, account):
    status, payload = request(base + "/api/auth/login",
                              {"username": account, "password": password_for(account)})
    if status != 200 or payload.get("tokenType") != "Bearer" or not payload.get("accessToken"):
        raise RuntimeError(f"测试账号 {account} 登录失败，HTTP {status}")
    return payload["accessToken"]


if __name__ == "__main__":
    import sys
    try:
        print(login(gateway_url(compose_config()), sys.argv[1]))
    except (RuntimeError, OSError, ValueError, KeyError, subprocess.SubprocessError) as error:
        sys.exit(f"FAIL {error}")
