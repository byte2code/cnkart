# CNKart Microservices

Spring Boot microservice suite for a simple e-commerce workflow with separate services for catalog items, inventory checks, order placement, order lifecycle tracking, and service discovery.

## Overview

CNKart started as a monolithic REST API and is now organized as a small microservice system. The split gives each business concern its own lifecycle, which makes the project easier to reason about and a better fit for distributed-systems learning.

The services work together like this:

- `item` manages catalog items.
- `inventory` checks stock availability for a requested SKU and quantity.
- `order` creates traceable order references, tracks order status, handles duplicate order submissions with an idempotency key, and checks inventory before confirming or rejecting the order.
- `discovery-server` acts as the Eureka registry for the service suite.

## Concepts / Features Covered

- Spring Boot REST APIs
- Spring Data JPA persistence
- MySQL-backed service databases
- Eureka service discovery
- OpenFeign-based service-to-service communication
- Hystrix fallback handling in the order flow
- Order lifecycle states: `PENDING`, `CONFIRMED`, `REJECTED`, and `FAILED`
- Idempotency key handling to avoid duplicate order creation on retries
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
- Spring Cloud Netflix Hystrix
- MySQL
- Lombok

## Services

| Service | Port | Responsibility |
| --- | --- | --- |
| `discovery-server` | `8761` | Eureka registry for service registration |
| `item` | `8081` | Create and list catalog items |
| `order` | `8082` | Create idempotent orders and track status after inventory validation |
| `inventory` | `8083` | Check stock for a SKU and quantity |

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
  "message": "Order confirmed"
}
```

Rejected response:

```json
{
  "orderReference": "ORD-6d6f7b78-1a7d-42de-a2df-4ccdc7f72cc5",
  "idempotencyKey": "checkout-1-user-42",
  "status": "REJECTED",
  "message": "Order rejected because item is not in stock"
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
| `CONFIRMED` | Inventory confirmed stock availability |
| `REJECTED` | Inventory responded but stock was unavailable |
| `FAILED` | Inventory validation failed because of an unavailable dependency or invalid order data |

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

## Flow Diagram

```mermaid
flowchart LR
    Client[Client] --> Order["order-service :8082"]
    Client --> Item["item-service :8081"]
    Client --> Inventory["inventory-service :8083"]

    Discovery["discovery-server :8761"] --- Item
    Discovery --- Inventory
    Discovery --- Order

    Order --> Pending["PENDING order with idempotency key"]
    Pending --> Inventory
    Inventory --> Confirmed["CONFIRMED"]
    Inventory --> Rejected["REJECTED"]
    Pending --> Failed["FAILED"]
    Item --> ItemDB[(item_service DB)]
    Inventory --> InvDB[(inventory_service DB)]
    Order --> OrderDB[(order_service DB)]
```

## Learning Highlights

- Converting a monolith into a microservice suite
- Using Eureka to register and discover services
- Calling one service from another with OpenFeign
- Keeping order placement resilient with Hystrix fallback
- Modeling order state transitions instead of only saving successful orders
- Using idempotency keys to make retry behavior safe for checkout APIs
- Returning traceable order references to support debugging and future event workflows
- Separating service data, config, and startup responsibility
- Practicing REST, JPA, and distributed-system wiring in one repo

## Notes

- The legacy monolithic CNKart codebase has been replaced by the microservice suite in this version.
- Configuration files are retained in each service folder for local setup.
- Build artifacts and IDE-specific files are intentionally excluded from the repository.
