#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

WITH_UNAVAILABLE=false
case "${1:-}" in
  "") ;;
  --with-unavailable) WITH_UNAVAILABLE=true ;;
  *) printf '用法：%s [--with-unavailable]\n' "$0" >&2; exit 2 ;;
esac

for tool in curl python3; do
  command -v "$tool" >/dev/null || { printf '缺少工具：%s\n' "$tool" >&2; exit 1; }
done

DOCKER_BIN="${DOCKER_BIN:-docker}"
if ! "$DOCKER_BIN" compose version >/dev/null 2>&1; then
  if [[ "$DOCKER_BIN" == docker ]] && command -v docker.exe >/dev/null; then
    DOCKER_BIN=docker.exe
  else
    printf 'Docker Compose 不可用，请启动 Docker Desktop 或检查 Docker 安装。\n' >&2
    exit 1
  fi
fi

PORTS="$("$DOCKER_BIN" compose config --format json | python3 -c '
import json, sys
services = json.load(sys.stdin)["services"]
print(" ".join(str(services[name]["ports"][0]["published"]) for name in
               ("pet-service", "doctor-service", "appointment-service")))
')"
read -r PET_PORT DOCTOR_PORT APPOINTMENT_PORT <<< "$PORTS"
PET_URL="${PET_BASE_URL:-http://localhost:$PET_PORT}"
DOCTOR_URL="${DOCTOR_BASE_URL:-http://localhost:$DOCTOR_PORT}"
APPOINTMENT_URL="${APPOINTMENT_BASE_URL:-http://localhost:$APPOINTMENT_PORT}"
RESPONSE_FILE="$(mktemp)"
DEPENDENCY_STOPPED=false

cleanup() {
  if "$DEPENDENCY_STOPPED"; then
    "$DOCKER_BIN" compose start pet-service >/dev/null 2>&1 || true
  fi
  rm -f -- "$RESPONSE_FILE"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

request() {
  local expected="$1" method="$2" url="$3" body="${4:-}" actual
  local args=(-sS --connect-timeout 5 --max-time 20 -o "$RESPONSE_FILE" -w '%{http_code}' -X "$method")
  if [[ -n "$body" ]]; then args+=(-H 'Content-Type: application/json' -d "$body"); fi
  if ! actual="$(curl "${args[@]}" "$url")"; then
    printf '请求失败：%s %s\n' "$method" "$url" >&2
    exit 1
  fi
  printf '\n%s %s -> HTTP %s\n' "$method" "$url" "$actual"
  cat "$RESPONSE_FILE"
  printf '\n'
  [[ "$actual" == "$expected" ]] || { printf '预期 HTTP %s\n' "$expected" >&2; exit 1; }
}

json_id() {
  python3 -c 'import json,sys; value=json.load(sys.stdin)["id"]; assert isinstance(value,int) and value>0; print(value)' < "$RESPONSE_FILE"
}

count_appointments() {
  request 200 GET "$APPOINTMENT_URL/api/appointments" >&2
  python3 -c 'import json,sys; print(len(json.load(sys.stdin)))' < "$RESPONSE_FILE"
}

check_health() {
  request 200 GET "$1/actuator/health"
  python3 -c 'import json,sys; assert json.load(sys.stdin)["status"]=="UP"' < "$RESPONSE_FILE"
}

check_health "$PET_URL"
check_health "$DOCTOR_URL"
check_health "$APPOINTMENT_URL"

request 201 POST "$PET_URL/api/pets" '{"name":"团团","species":"猫","age":2,"ownerName":"孔磊","ownerPhone":"13800000000","healthNotes":"第六周跨服务验证"}'
PET_ID="$(json_id)"
request 200 GET "$PET_URL/api/pets/$PET_ID"

DOCTOR_CODE="W06$(date +%s%N)"
request 201 POST "$DOCTOR_URL/api/doctors" "{\"code\":\"$DOCTOR_CODE\",\"name\":\"林医生\",\"department\":\"内科\",\"dayOfWeek\":\"MONDAY\",\"startTime\":\"09:00\",\"endTime\":\"17:00\",\"accepting\":true}"
DOCTOR_ID="$(json_id)"

APPOINTMENT_TIME="$(python3 -c '
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
today = datetime.now(ZoneInfo("Asia/Shanghai")).date()
day = today + timedelta(days=(-today.weekday()) % 7 or 7)
print(day.isoformat()+"T09:00:00")
')"
BODY="{\"petId\":$PET_ID,\"doctorId\":$DOCTOR_ID,\"appointmentTime\":\"$APPOINTMENT_TIME\",\"reason\":\"第六周跨服务预约验证\"}"
request 201 POST "$APPOINTMENT_URL/api/appointments" "$BODY"
APPOINTMENT_ID="$(json_id)"
request 200 GET "$APPOINTMENT_URL/api/appointments/$APPOINTMENT_ID"
request 409 POST "$APPOINTMENT_URL/api/appointments" "$BODY"
request 200 POST "$APPOINTMENT_URL/api/appointments/$APPOINTMENT_ID/confirm"
request 200 POST "$APPOINTMENT_URL/api/appointments/$APPOINTMENT_ID/cancel"
request 201 POST "$APPOINTMENT_URL/api/appointments" "$BODY"

BEFORE="$(count_appointments)"
MISSING_BODY="{\"petId\":9223372036854775807,\"doctorId\":$DOCTOR_ID,\"appointmentTime\":\"$APPOINTMENT_TIME\",\"reason\":\"验证依赖资源不存在\"}"
request 404 POST "$APPOINTMENT_URL/api/appointments" "$MISSING_BODY"
AFTER="$(count_appointments)"
[[ "$BEFORE" == "$AFTER" ]] || { printf '失败请求写入了本地数据\n' >&2; exit 1; }
printf 'PASS：依赖资源不存在返回404，预约数量保持 %s。\n' "$AFTER"

if "$WITH_UNAVAILABLE"; then
  DEPENDENCY_STOPPED=true
  "$DOCKER_BIN" compose stop pet-service
  BEFORE="$(count_appointments)"
  request 503 POST "$APPOINTMENT_URL/api/appointments" "$BODY"
  AFTER="$(count_appointments)"
  [[ "$BEFORE" == "$AFTER" ]] || { printf '依赖不可用时写入了本地数据\n' >&2; exit 1; }
  printf 'PASS：宠物服务不可用返回503，预约数量保持 %s。\n' "$AFTER"
  "$DOCKER_BIN" compose start pet-service
  RESTORED=false
  for attempt in {1..60}; do
    if curl -fsS --connect-timeout 2 --max-time 5 "$PET_URL/actuator/health" > "$RESPONSE_FILE" 2>/dev/null; then
      if python3 -c 'import json,sys; assert json.load(sys.stdin)["status"]=="UP"' < "$RESPONSE_FILE" 2>/dev/null; then
        RESTORED=true
        break
      fi
    fi
    sleep 1
  done
  "$RESTORED" || { printf '宠物服务未恢复健康\n' >&2; exit 1; }
  DEPENDENCY_STOPPED=false
  check_health "$PET_URL"
fi

printf '\nPASS：健康检查、跨服务预约、冲突拦截、确认取消与失败不写入验证通过。\n'
