# CNKart Service Startup

This repository can be started either manually or with Docker Compose.

## Docker Compose (Recommended)

Use Docker Compose when you want the full stack with MySQL, Kafka, Eureka, item, inventory, and order services running together.

```bash
docker compose up --build
```

The `docker-compose.override.yml` provides sensible defaults for all environment variables — no `.env` file is needed for a first run.

The exposed ports are:

- `8761` for `discovery-server`
- `8081` for `item`
- `8082` for `order`
- `8083` for `inventory`
- `3306` for MySQL
- `9092` for Kafka

## Manual Startup

Use manual startup when you want to run the services one by one from your IDE or terminal.

1. Start MySQL and create the databases `item_service`, `inventory_service`, and `order_service`.
2. Start Kafka and make sure it is reachable on `localhost:9092`.
3. Start `discovery-server` on port `8761`.
4. Start `inventory` on port `8083`.
5. Start `item` on port `8081`.
6. Start `order` on port `8082`.

## Request Flow

The recommended order flow is:

1. A client creates an order through the `order` service.
2. The order service stores the request in `PENDING` state and emits `OrderCreated` to `cnkart.order.events`.
3. The order service asks `inventory` to reserve stock via OpenFeign (protected by a Resilience4j circuit breaker).
4. Inventory either reserves the quantity and emits `InventoryReserved`, or rejects the request and emits `InventoryRejected` to `cnkart.inventory.events`.
5. If the reservation succeeds, the order service marks the order `CONFIRMED` and emits `OrderConfirmed`.
6. The order service also listens to `cnkart.inventory.events` via a Kafka consumer stub, closing the event loop.

## Verifying the System

After all services are running, execute the smoke test:

```bash
./scripts/smoke-test.sh
```

The script exercises item creation, inventory checks, order placement, idempotency, Swagger docs, and the Eureka dashboard. It prints a coloured pass/fail summary.

## API Documentation

Swagger UI is available for:

- **Order service**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **Inventory service**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

## Notes

- The Docker Compose setup uses environment variables so the services can also run outside containers without editing source files.
- The reservation flow is intentionally kept as a separate step so it is easy to extend with downstream consumers later.
- All business services return structured error responses via `@ControllerAdvice`.
