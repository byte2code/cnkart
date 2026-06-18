# CNKart Microservices

Spring Boot microservice suite for a simple e-commerce workflow with separate services for catalog items, inventory reservation, order placement, order lifecycle tracking, and service discovery.

## Overview

CNKart started as a monolithic REST API and is now organized as a small microservice system. The split gives each business concern its own lifecycle, which makes the project easier to reason about and a better fit for distributed-systems learning.

The services work together like this:

- `item` manages catalog items.
- `inventory` checks stock availability and reserves stock for an order before confirmation.
- `order` creates traceable order references, tracks order status, handles duplicate order submissions with an idempotency key, and confirms orders only after inventory reservation succeeds.
- `discovery-server` acts as the Eureka registry for the service suite.

## Concepts / Features Covered

- Spring Boot REST APIs
- Spring Data JPA persistence
- MySQL-backed service databases
- Eureka service discovery
- OpenFeign-based service-to-service communication
- Resilience4j circuit breaker fallback handling in the order flow
- Order lifecycle states: `PENDING`, `CONFIRMED`, `REJECTED`, and `FAILED`
- Idempotency key handling to avoid duplicate order creation on retries
- Inventory reservation before order confirmation
- Pessimistic locking during stock reservation to reduce overselling risk
- Kafka domain events for order creation, inventory reservation, inventory rejection, and order confirmation
- Generated order references for easier order tracing
- Service-local configuration files for each module
- Independent service startup and runtime lifecycle

## Tech Stack

- Java 8
- Spring Boot 2.7.13
- Spring Cloud 2021.0.8
- Spring Web
- Spring Data JPA
- Spring Cloud Netflix Eureka
- Spring Cloud OpenFeign
- Spring Cloud Circuit Breaker Resilience4j
- Spring Kafka
- MySQL
- Docker
- Docker Compose
- Lombok

## Services

| Service | Port | Responsibility |
| --- | --- | --- |
| `discovery-server` | `8761` | Eureka registry for service registration |
| `item` | `8081` | Create and list catalog items |
| `order` | `8082` | Create idempotent orders and track status after inventory reservation |
| `inventory` | `8083` | Check stock and reserve available quantity for orders |

## Event Flow

| Event | Producer | Meaning |
| --- | --- | --- |
| `OrderCreated` | `order` | An order was accepted and stored in `PENDING` state |
| `InventoryReserved` | `inventory` | Stock was reserved successfully for the order |
| `InventoryRejected` | `inventory` | Reservation was declined because stock was unavailable or invalid |
| `OrderConfirmed` | `order` | The order was confirmed after inventory reservation succeeded |

## Example API Calls

### Create an item

```bash
curl -X POST http://localhost:8081/api/item \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "description": "2.4 GHz ergonomic mouse",
    "price": 799.00
  }'
```

Expected result:

```text
201 Created
```

### List items

```bash
curl http://localhost:8081/api/item
```

Sample response:

```json
[
  {
    "id": 1,
    "name": "Wireless Mouse",
    "description": "2.4 GHz ergonomic mouse",
    "price": 799.00
  }
]
```

### Check inventory

```bash
curl "http://localhost:8083/api/inventory?skuCode=1&qty=2"
```

Sample response:

```json
true
```

### Reserve inventory

```bash
curl -X POST http://localhost:8083/api/inventory/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
    "skuCode": "1",
    "quantity": 2
  }'
```

Sample response:

```json
{
  "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
  "skuCode": "1",
  "requestedQuantity": 2,
  "availableQuantity": 8,
  "reserved": true,
  "message": "Inventory reserved successfully"
}
```

### Place an order

```bash
curl -X POST http://localhost:8082/api/order \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "1",
    "price": 799.00,
    "quantity": 2,
    "idempotencyKey": "checkout-1-user-42"
  }'
```

Success response:

```json
{
  "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
  "idempotencyKey": "checkout-1-user-42",
  "status": "CONFIRMED",
  "message": "Order confirmed after inventory reservation"
}
```

Rejected response:

```json
{
  "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
  "idempotencyKey": "checkout-1-user-42",
  "status": "REJECTED",
  "message": "Insufficient stock available for reservation"
}
```

Duplicate request response:

```json
{
  "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
  "idempotencyKey": "checkout-1-user-42",
  "status": "CONFIRMED",
  "message": "Duplicate order request detected, returning existing order status"
}
```

Use the same `idempotencyKey` when retrying the same checkout request. The order service returns the existing order instead of creating a duplicate record.

## Order Lifecycle

| Status | Meaning |
| --- | --- |
| `PENDING` | Order command has been accepted and stored before inventory validation |
| `CONFIRMED` | Inventory reservation succeeded and stock was deducted |
| `REJECTED` | Inventory reservation was declined because stock was unavailable or invalid |
| `FAILED` | Inventory reservation failed because of an unavailable dependency or invalid order data |

## Project Structure

```text
cnkart/
├── discovery-server/
├── item/
├── inventory/
├── order/
├── README.md
├── CHANGELOG.md
└── .gitignore
```

Each service keeps its own `pom.xml`, `mvnw`, source tree, and configuration file so it can be built and run independently.

## How to Run

1. Create the MySQL databases used by the services: `item_service`, `inventory_service`, and `order_service`.
2. Start `discovery-server` on port `8761`.
3. Start `inventory` on port `8083`.
4. Start `item` on port `8081`.
5. Start `order` on port `8082`.
6. Call the endpoints above from Postman, curl, or any REST client.

The local configuration files currently point to `localhost` MySQL settings, so update the database username and password if your environment differs.

For a repeatable local environment, use Docker Compose:

- `docker compose up --build`
- [SERVICE_STARTUP.md](/Users/bipinverma/Documents/myProjects/cnkart/SERVICE_STARTUP.md)

## Docker Compose

The repo includes Dockerfiles for each service and a root `docker-compose.yml` that starts:

- `discovery-server`
- `item`
- `inventory`
- `order`
- MySQL
- Kafka

The compose setup uses environment variables so the same services can run in containers or directly from your IDE without code changes.

## Flow Diagram

```mermaid
flowchart LR
    Client[Client] --> Item["item-service :8081"]
    Client --> Order["order-service :8082"]

    subgraph Compose["Docker Compose Local Stack"]
        Discovery["discovery-server :8761"]
        Inventory["inventory-service :8083"]
        MySQL[(MySQL :3306)]
        Kafka[(Kafka :9092)]
    end

    Item --> Discovery
    Order --> Discovery
    Inventory --> Discovery

    Order --> Pending["PENDING order with idempotency key"]
    Pending --> Reservation["POST /api/inventory/reservations"]
    Reservation --> Inventory
    Pending --> OrderCreated["Kafka: OrderCreated"]
    Inventory --> Confirmed["CONFIRMED after stock deduction"]
    Inventory --> Rejected["REJECTED without stock deduction"]
    Inventory --> InventoryReserved["Kafka: InventoryReserved"]
    Inventory --> InventoryRejected["Kafka: InventoryRejected"]
    Confirmed --> OrderConfirmed["Kafka: OrderConfirmed"]
    Pending --> Failed["FAILED"]
    Item --> MySQL
    Inventory --> MySQL
    Order --> MySQL
    Order --> Kafka
    Inventory --> Kafka
```

## Learning Highlights

- Converting a monolith into a microservice suite
- Using Eureka to register and discover services
- Calling one service from another with OpenFeign
- Keeping order placement resilient with Resilience4j fallback
- Modeling order state transitions instead of only saving successful orders
- Using idempotency keys to make retry behavior safe for checkout APIs
- Reserving inventory before confirmation instead of only checking stock availability
- Applying pessimistic locking during reservation to protect stock updates
- Returning traceable order references to support debugging and future event workflows
- Emitting Kafka events from the order and inventory services for downstream workflows
- Separating service data, config, and startup responsibility
- Practicing REST, JPA, and distributed-system wiring in one repo

## Notes

- The legacy monolithic CNKart codebase has been replaced by the microservice suite in this version.
- Configuration files are retained in each service folder for local setup.
- Build artifacts and IDE-specific files are intentionally excluded from the repository.
