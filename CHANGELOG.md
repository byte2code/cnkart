# Changelog

All notable changes to this project are documented in this file.

## [v7.2.0] - 2026-06-18

### Summary
Replaced Hystrix with Resilience4j circuit breaker in the order service.

### Highlights
- Replaced `spring-cloud-starter-netflix-hystrix` with `spring-cloud-starter-circuitbreaker-resilience4j` in the order service.
- Removed Hystrix annotations (`@EnableHystrix`, `@EnableHystrixDashboard`, `@HystrixCommand`) and configuration blocks.
- Added `@CircuitBreaker` annotation and updated the fallback method signature in `OrderController`.
- Enabled OpenFeign circuit breaker globally for the order service.

## [v7.1.0] - 2026-06-18

### Summary
Fixed Feign client query parameter interpolation bug.

### Highlights
- Removed query parameter placeholders from the `@GetMapping` annotation inside `InventoryService` interface in the `order` service.
- Added explicit parameter names inside `@RequestParam` annotations to ensure query parameters map correctly during remote calls.

## [v7.0.0] - 2026-05-28

Added a Docker Compose-based local runtime for the CNKart microservice suite.

- Added Dockerfiles for `discovery-server`, `item`, `inventory`, and `order`.
- Added a root `docker-compose.yml` that starts MySQL, Kafka, Eureka, and all CNKart services together.
- Added environment-variable-based configuration so the same services can run locally or in containers without code changes.
- Added a root `SERVICE_STARTUP.md` guide covering Docker Compose and manual service startup order.
- Updated the README with the compose-aware flow diagram and startup instructions.

## [v6.0.0] - 2026-05-27

Added a Kafka event trail around the existing order and inventory reservation workflow.

- Added `OrderCreated` and `OrderConfirmed` events from the order service.
- Added `InventoryReserved` and `InventoryRejected` events from the inventory service.
- Added lightweight Kafka publishers that serialize event payloads as JSON strings.
- Kept the reservation flow intact while making the system easier to extend with downstream consumers later.
- Added unit-test coverage for event publishing calls in both the order and inventory services.
- Added Kafka producer configuration placeholders in the service configuration files.

## [v5.0.0] - 2026-05-21

Changed the order flow from a simple inventory check into an inventory reservation workflow.

- Added `POST /api/inventory/reservations` to reserve stock for a specific order reference.
- Added inventory reservation request and response DTOs in the inventory and order services.
- Updated order placement so orders are confirmed only after stock is successfully reserved.
- Added pessimistic locking while loading inventory rows for reservation to reduce overselling risk during concurrent checkout requests.
- Preserved the existing stock-check endpoint for read-only availability checks.
- Added focused inventory service tests for successful reservation, insufficient stock, missing SKU, and invalid quantity scenarios.
- Updated order service tests to validate reservation-based confirmation, rejection, failure, and idempotency behavior.

## [v4.0.0] - 2026-05-20

Expanded the order service from a simple stock-check-and-save flow into a traceable order lifecycle workflow.

- Added order lifecycle states: `PENDING`, `CONFIRMED`, `REJECTED`, and `FAILED`.
- Added generated order references using the `ORD-` prefix for easier order tracing.
- Added idempotency key support so repeated checkout retries can return the existing order status instead of creating duplicate orders.
- Changed the order API response from plain text to a structured response containing order reference, idempotency key, status, and message.
- Persisted rejected and failed order attempts so the order service records more than only successful orders.
- Added focused order service tests for confirmed, rejected, failed, and duplicate idempotency-key scenarios.

## [v3.0.0] - 2026-05-11

Converted the CNKart repo into a microservice suite with discovery, item, inventory, and order services.

- Added a standalone `discovery-server` service for Eureka registration.
- Added an `item` service for catalog item creation and listing.
- Added an `inventory` service for stock availability checks.
- Added an `order` service with Feign-based inventory checks and Hystrix fallback support.
- Retained service-local configuration files for each module.

## [v2.0.0] - 2026-04-06

Second version of the CNKart API that expanded the project with query-based filtering for item descriptions, price thresholds, and category ordering.

- Added `GET /api/item/desc/{desc}` for description-prefix item lookup.
- Added `GET /api/details/item/price/{price}` for filtering item details above a price.
- Added `GET /api/details/item/category/{category}` for category-based detail lookup ordered by price.
- Introduced native-query and named-query examples in the entity layer.
- Updated the API route structure to use `/api` and `/api/details` prefixes.

## [v1.0.0] - 2026-04-04

Initial publication of the CNKart API as a clean, portfolio-ready Spring Boot REST project.

- Added a recruiter-friendly README with API overview, features, run steps, and project structure.
- Added a changelog for future version tracking.
- Cleaned IDE files and build artifacts before publishing.
- Preserved the original item and item-details management workflow built on Spring Data JPA and MySQL.
