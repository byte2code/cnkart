# CNKart Service Startup

This repository can be started either manually or with Docker Compose.

## Docker Compose

Use Docker Compose when you want the full stack with MySQL, Kafka, Eureka, item, inventory, and order services running together.

```bash
docker compose up --build
```

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
2. The order service stores the request in `PENDING` state and emits `OrderCreated`.
3. The order service asks `inventory` to reserve stock.
4. Inventory either reserves the quantity and emits `InventoryReserved`, or rejects the request and emits `InventoryRejected`.
5. If the reservation succeeds, the order service marks the order `CONFIRMED` and emits `OrderConfirmed`.

## Notes

- The Docker Compose setup uses environment variables so the services can also run outside containers without editing source files.
- The reservation flow is intentionally kept as a separate step so it is easy to extend with downstream consumers later.
