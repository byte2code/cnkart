# Changelog

All notable changes to this project are documented in this file.

## [v8.0.0] - 2026-07-11

### Summary
Evolved CNKart toward a production-ready microservice system by adding an observability stack, a Testcontainers integration suite, API Gateway security hardening, performance baselining, and cross-repo architectural documentation.

### Highlights
- Added Spring Boot Actuator, Spring Cloud Sleuth, and Zipkin to all services (`item`, `inventory`, `order`, `api-gateway`) for distributed tracing and observability.
- Added `zipkin` service to the Docker Compose stack.
- Replaced H2 in-memory test databases with Testcontainers (MySQL & Kafka) across all module integration tests (`@SpringBootTest`).
- Added `SecurityAuditFilter` to the `api-gateway` to log incoming requests (IP, Path, Method).
- Added a `k6-load-test.js` script to baseline order placement throughput and latency.
- Updated `README.md` with cross-repo integration flow diagrams showing how CNKart integrates with Hotel, Telecom, and Logistics ecosystems via event-driven architecture.

## [v7.7.0] - 2026-06-29

### Summary
Added a curl-based smoke test script and finalized all project documentation for the v7.x release.

### Highlights
- Added `scripts/smoke-test.sh` — an end-to-end smoke test that exercises all public endpoints (item CRUD, inventory check + reservation, order placement + idempotency, Swagger UI, Eureka dashboard) with coloured pass/fail output.
- Rewrote `README.md` with:
  - Quick Start section for clone-and-run in 2 minutes.
  - Mermaid architecture diagram showing all services, infrastructure, and communication paths.
  - Mermaid sequence diagram for the full order placement flow (including circuit breaker fallback).
  - Expanded event flow table with Kafka topics, producers, and consumers.
  - Event payload JSON examples for all 4 domain events.
  - Swagger UI quick-reference table for order and inventory services.
  - Error response example using the structured `ApiError` format.
  - Smoke test usage documentation.
  - Updated project structure tree with `scripts/`, `docker/`, and `docker-compose.override.yml`.
  - Updated tech stack, features, and learning highlights to reflect all v7.x additions.

## [v7.6.0] - 2026-06-29

### Summary
Added `docker-compose.override.yml` with environment variable defaults to simplify local setup.

### Highlights
- Created `docker-compose.override.yml` which exposes common application settings (like `MYSQL_ROOT_PASSWORD`, `SERVER_PORT`, and `SPRING_DATASOURCE_URL`) as overridable environment variables with fallback defaults.
- This allows developers to clone the repository and run `docker compose up` within minutes without needing to modify the base `docker-compose.yml` file.

## [v7.5.0] - 2026-06-29

### Summary
Added Kafka consumer stub in the `order` service to close the event loop.

### Highlights
- Added `InventoryEventListener` class with `@KafkaListener` to consume events from `cnkart.inventory.events`.
- Configured Kafka consumer deserializers (`StringDeserializer`) and `group-id` in `order-service`'s `application.yml`.

## [v7.4.0] - 2026-06-28

### Summary
Added Swagger / OpenAPI documentation to the order and inventory services.

### Highlights
- Added `springdoc-openapi-ui` (v1.7.0) dependency to `order` and `inventory` services.
- Created `OpenApiConfig` classes in both services with service-specific API titles and descriptions.
- Annotated `OrderController` with `@Tag`, `@Operation`, and `@ApiResponses` for the place-order endpoint.
- Annotated `InventoryController` with `@Tag`, `@Operation`, `@Parameter`, and `@ApiResponses` for the stock-check and reservation endpoints.
- Swagger UI available at `/swagger-ui.html` and OpenAPI spec at `/v3/api-docs` for both services.

## [v7.3.0] - 2026-06-18

### Summary
Added `@ControllerAdvice` exception handling and a structured `ApiError` representation across all 3 business services.

### Highlights
- Created `ApiError` class in `com.cnkart.item.dto`, `com.cnkart.inventory.dto`, and `com.cnkart.order.dto` with fields for timestamp, status, error reason, message, and request path.
- Created `GlobalExceptionHandler` annotated with `@ControllerAdvice` in `com.cnkart.item.exception`, `com.cnkart.inventory.exception`, and `com.cnkart.order.exception` to handle `IllegalArgumentException` and generic `Exception`.
- Resolved Lombok compilation errors in the `item` service by adding the `maven-compiler-plugin` configuration and setting `<lombok.version>` to `1.18.36` in `item/pom.xml`.
- Configured H2 database properties in `ItemApplicationTests` and added `h2` test dependency to `item/pom.xml` so the item service test context loads successfully without a running MySQL instance.

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
