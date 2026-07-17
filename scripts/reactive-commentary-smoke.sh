#!/usr/bin/env bash

set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
server_repo="${repo_root}/../Wumpus-server"
commentary_url="${WUMPUS_COMMENTARY_URL:-http://localhost:8080/api/commentary}"
health_url="${WUMPUS_COMMENTARY_HEALTH_URL:-http://localhost:8080/q/health}"
start_server_mode="${WUMPUS_SMOKE_START_SERVER:-auto}" # auto | always | never
run_client_tests="${WUMPUS_SMOKE_RUN_CLIENT_TESTS:-true}"

server_pid=""
server_started_by_script="false"

cleanup() {
  if [[ "$server_started_by_script" == "true" && -n "$server_pid" ]]; then
    kill "$server_pid" >/dev/null 2>&1 || true
    wait "$server_pid" 2>/dev/null || true
  fi
}
trap cleanup EXIT

is_server_healthy() {
  curl -fsS "$health_url" >/dev/null 2>&1
}

start_server() {
  if [[ ! -d "$server_repo" ]]; then
    echo "Server repo not found at $server_repo"
    exit 1
  fi

  echo "Starting local commentary server from $server_repo ..."
  (
    cd "$server_repo"
    WUMPUS_LLM_PROVIDER="${WUMPUS_LLM_PROVIDER:-fallback}" \
      ./mvnw -q quarkus:dev -Dquarkus.http.port=8080
  ) >/tmp/wumpus-commentary-smoke-server.log 2>&1 &

  server_pid="$!"
  server_started_by_script="true"

  for _ in {1..60}; do
    if is_server_healthy; then
      return 0
    fi
    sleep 1
  done

  echo "Server failed to become healthy at $health_url"
  echo "Last server logs:"
  tail -n 40 /tmp/wumpus-commentary-smoke-server.log || true
  exit 1
}

ensure_server() {
  if is_server_healthy; then
    echo "Using existing commentary server at $commentary_url"
    return 0
  fi

  case "$start_server_mode" in
    auto|always)
      start_server
      ;;
    never)
      echo "No running server found at $health_url and WUMPUS_SMOKE_START_SERVER=never."
      exit 1
      ;;
    *)
      echo "Invalid WUMPUS_SMOKE_START_SERVER value: $start_server_mode (expected auto|always|never)"
      exit 1
      ;;
  esac
}

run_commentary_smoke() {
  local payload
  payload='{
    "action":"SHOOT",
    "actionIntent":"SHOOT_THROUGH_CAVES",
    "intendedTargetRoom":9,
    "nominatedPath":[7,9],
    "targetRoom":7,
    "outcome":"SHOT_MISSED",
    "playerRoom":4,
    "adjacentRooms":[1,2,3],
    "hazardWarnings":["You feel a cold draft from a nearby cave."],
    "arrowsRemaining":3,
    "movesTaken":5,
    "previousActionSummaries":["MOVE -> SAFE @ 5"]
  }'

  local response
  response="$(curl -fsS -X POST "$commentary_url" \
    -H 'Content-Type: application/json' \
    -d "$payload")"

  python3 - "$response" <<'PY'
import json
import sys

response = json.loads(sys.argv[1])
commentary = response.get("commentary")
if not isinstance(commentary, str) or not commentary.strip():
    raise SystemExit("Commentary response is missing a non-empty 'commentary' field.")

print("Commentary response OK")
print(f"fallback={response.get('fallback')}")
print(f"commentary={commentary.strip()}")
PY
}

run_client_smoke_tests() {
  if [[ "$run_client_tests" != "true" ]]; then
    echo "Skipping client smoke tests (WUMPUS_SMOKE_RUN_CLIENT_TESTS=$run_client_tests)"
    return 0
  fi

  echo "Running focused client commentary tests ..."
  "$repo_root/gradlew" -p "$repo_root" test \
    --tests "za.co.sww.game.wumpus.commentary.CommentaryDispatcherTest" \
    --tests "za.co.sww.game.wumpus.commentary.NoopCommentaryClientTest" \
    --tests "za.co.sww.game.wumpus.service.HuntServiceCommentarySnapshotTest"
}

ensure_server
run_commentary_smoke
run_client_smoke_tests

echo "Reactive commentary smoke checks passed."
