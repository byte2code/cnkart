#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# CNKart Smoke Test
# ---------------------------------------------------------------------------
# Proves the system is running by exercising every public endpoint in order:
#   1. Item service   → create + list items
#   2. Inventory      → stock check
#   3. Order service  → place order (with reservation) + idempotency check
#   4. Swagger UI     → verify OpenAPI docs are reachable
#
# Prerequisites:
#   - All services running (via `docker compose up` or manually)
#   - curl and jq installed
#
# Usage:
#   chmod +x scripts/smoke-test.sh
#   ./scripts/smoke-test.sh
#   ./scripts/smoke-test.sh --base-url http://myhost  # custom host
# ---------------------------------------------------------------------------
set -euo pipefail

# ── Defaults ──────────────────────────────────────────────────────────────────
BASE_URL="${1:-http://localhost}"
ITEM_URL="${BASE_URL}:8081"
ORDER_URL="${BASE_URL}:8082"
INVENTORY_URL="${BASE_URL}:8083"
DISCOVERY_URL="${BASE_URL}:8761"

PASS=0
FAIL=0
TOTAL=0

# ── Helpers ───────────────────────────────────────────────────────────────────
green()  { printf "\033[32m%s\033[0m\n" "$*"; }
red()    { printf "\033[31m%s\033[0m\n" "$*"; }
yellow() { printf "\033[33m%s\033[0m\n" "$*"; }
bold()   { printf "\033[1m%s\033[0m\n" "$*"; }

assert_status() {
  local label="$1" expected="$2" actual="$3"
  TOTAL=$((TOTAL + 1))
  if [ "$actual" -eq "$expected" ]; then
    green "  ✔ $label  (HTTP $actual)"
    PASS=$((PASS + 1))
  else
    red  "  ✘ $label  (expected $expected, got $actual)"
    FAIL=$((FAIL + 1))
  fi
}

assert_json_field() {
  local label="$1" body="$2" field="$3" expected="$4"
  TOTAL=$((TOTAL + 1))
  actual=$(echo "$body" | jq -r "$field" 2>/dev/null || echo "PARSE_ERROR")
  if [ "$actual" = "$expected" ]; then
    green "  ✔ $label  ($field = $actual)"
    PASS=$((PASS + 1))
  else
    red  "  ✘ $label  ($field expected '$expected', got '$actual')"
    FAIL=$((FAIL + 1))
  fi
}

wait_for_service() {
  local url="$1" name="$2" retries=30
  printf "  Waiting for %s at %s " "$name" "$url"
  for i in $(seq 1 $retries); do
    if curl -sf -o /dev/null "$url" 2>/dev/null; then
      green "ready"
      return 0
    fi
    printf "."
    sleep 2
  done
  red " timed out after $((retries * 2))s"
  return 1
}

# ── Banner ────────────────────────────────────────────────────────────────────
echo ""
bold "╔══════════════════════════════════════════════════════════════╗"
bold "║               CNKart Smoke Test Suite                       ║"
bold "╚══════════════════════════════════════════════════════════════╝"
echo ""

# ── 0. Readiness checks ──────────────────────────────────────────────────────
bold "⏳  Service readiness checks"
wait_for_service "${DISCOVERY_URL}" "discovery-server" || { red "Aborting: discovery-server not reachable."; exit 1; }
wait_for_service "${ITEM_URL}/api/item" "item-service" || { red "Aborting: item-service not reachable."; exit 1; }
wait_for_service "${INVENTORY_URL}/api/inventory?skuCode=0&qty=1" "inventory-service" || { red "Aborting: inventory-service not reachable."; exit 1; }
echo ""

# ── 1. Item Service ──────────────────────────────────────────────────────────
bold "📦  Test Group: Item Service"

# 1a. Create an item
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "${ITEM_URL}/api/item" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smoke Test Mouse",
    "description": "Created by smoke-test.sh",
    "price": 499.00
  }')
assert_status "POST /api/item  → create item" 201 "$STATUS"

# 1b. List items
RESPONSE=$(curl -s -w "\n%{http_code}" "${ITEM_URL}/api/item")
STATUS=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
assert_status "GET  /api/item  → list items" 200 "$STATUS"

ITEM_COUNT=$(echo "$BODY" | jq 'length' 2>/dev/null || echo 0)
TOTAL=$((TOTAL + 1))
if [ "$ITEM_COUNT" -gt 0 ]; then
  green "  ✔ Item list is non-empty  (count = $ITEM_COUNT)"
  PASS=$((PASS + 1))
else
  red  "  ✘ Item list is empty"
  FAIL=$((FAIL + 1))
fi
echo ""

# ── 2. Inventory Service ────────────────────────────────────────────────────
bold "📊  Test Group: Inventory Service"

# 2a. Check stock (skuCode=1, qty=1)
RESPONSE=$(curl -s -w "\n%{http_code}" "${INVENTORY_URL}/api/inventory?skuCode=1&qty=1")
STATUS=$(echo "$RESPONSE" | tail -1)
assert_status "GET  /api/inventory?skuCode=1&qty=1  → stock check" 200 "$STATUS"

# 2b. Reserve stock
RESERVE_REF="ORD-SMOKE-$(date +%s)"
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -X POST "${INVENTORY_URL}/api/inventory/reservations" \
  -H "Content-Type: application/json" \
  -d "{
    \"orderReference\": \"${RESERVE_REF}\",
    \"skuCode\": \"1\",
    \"quantity\": 1
  }")
STATUS=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
assert_status "POST /api/inventory/reservations  → reserve stock" 201 "$STATUS"
assert_json_field "Reservation response has orderReference" "$BODY" ".orderReference" "$RESERVE_REF"
echo ""

# ── 3. Order Service ────────────────────────────────────────────────────────
bold "🛒  Test Group: Order Service"

IDEM_KEY="smoke-test-$(date +%s)"

# 3a. Place order
RESPONSE=$(curl -s -w "\n%{http_code}" \
  -X POST "${ORDER_URL}/api/order" \
  -H "Content-Type: application/json" \
  -d "{
    \"skuCode\": \"1\",
    \"price\": 499.00,
    \"quantity\": 1,
    \"idempotencyKey\": \"${IDEM_KEY}\"
  }")
STATUS=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
assert_status "POST /api/order  → place order" 201 "$STATUS"
assert_json_field "Order has idempotencyKey" "$BODY" ".idempotencyKey" "$IDEM_KEY"

ORDER_STATUS=$(echo "$BODY" | jq -r '.status' 2>/dev/null || echo "UNKNOWN")
TOTAL=$((TOTAL + 1))
if [ "$ORDER_STATUS" = "CONFIRMED" ] || [ "$ORDER_STATUS" = "REJECTED" ]; then
  green "  ✔ Order resolved to terminal state  (status = $ORDER_STATUS)"
  PASS=$((PASS + 1))
else
  red  "  ✘ Unexpected order status  (status = $ORDER_STATUS)"
  FAIL=$((FAIL + 1))
fi

# 3b. Idempotent retry
RESPONSE2=$(curl -s -w "\n%{http_code}" \
  -X POST "${ORDER_URL}/api/order" \
  -H "Content-Type: application/json" \
  -d "{
    \"skuCode\": \"1\",
    \"price\": 499.00,
    \"quantity\": 1,
    \"idempotencyKey\": \"${IDEM_KEY}\"
  }")
STATUS2=$(echo "$RESPONSE2" | tail -1)
BODY2=$(echo "$RESPONSE2" | sed '$d')
assert_status "POST /api/order  → idempotent retry" 201 "$STATUS2"
assert_json_field "Idempotent retry returns same status" "$BODY2" ".status" "$ORDER_STATUS"
assert_json_field "Idempotent retry message" "$BODY2" ".message" "Duplicate order request detected, returning existing order status"
echo ""

# ── 4. Swagger / OpenAPI ────────────────────────────────────────────────────
bold "📖  Test Group: Swagger / OpenAPI"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${ORDER_URL}/v3/api-docs")
assert_status "GET  order /v3/api-docs  → OpenAPI spec" 200 "$STATUS"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${INVENTORY_URL}/v3/api-docs")
assert_status "GET  inventory /v3/api-docs  → OpenAPI spec" 200 "$STATUS"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${ORDER_URL}/swagger-ui/index.html")
assert_status "GET  order /swagger-ui  → Swagger UI" 200 "$STATUS"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${INVENTORY_URL}/swagger-ui/index.html")
assert_status "GET  inventory /swagger-ui  → Swagger UI" 200 "$STATUS"
echo ""

# ── 5. Discovery Server ────────────────────────────────────────────────────
bold "🔍  Test Group: Discovery Server"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${DISCOVERY_URL}")
assert_status "GET  discovery-server /  → Eureka dashboard" 200 "$STATUS"
echo ""

# ── Summary ──────────────────────────────────────────────────────────────────
bold "═══════════════════════════════════════════════════════════════"
if [ "$FAIL" -eq 0 ]; then
  green "  ALL $TOTAL CHECKS PASSED ✔"
else
  red   "  $FAIL / $TOTAL CHECKS FAILED ✘"
  yellow "  ($PASS passed)"
fi
bold "═══════════════════════════════════════════════════════════════"
echo ""

exit "$FAIL"
