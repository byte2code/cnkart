# CNKart Microservices

Spring Boot microservice suite for a simple e-commerce workflow. The repository now contains separate services for catalog items, inventory checks, order placement, and service discovery.

## Overview

This version evolves the original CNKart application from a single monolithic REST API into a multi-service architecture. Each service has its own Spring Boot project, configuration, and lifecycle, which makes the repo a better fit for distributed-systems learning and demo use.

## Services

- `discovery-server` on port `8761`
- `item` on port `8081`
- `order` on port `8082`
- `inventory` on port `8083`

## What Each Service Does

- `item` exposes item creation and item listing APIs.
- `inventory` checks stock availability for a requested SKU and quantity.
- `order` places orders and coordinates inventory verification with a Hystrix fallback.
- `discovery-server` acts as the Eureka registry for the service suite.

## Shared Concepts

- Spring Boot REST services
- Spring Data JPA persistence
- MySQL-backed runtime configuration
- Eureka client/server registration
- OpenFeign-based service-to-service calls
- Hystrix fallback support for order placement
- Separate `application.yml` / `application.properties` files per service

## Repository Layout

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

## Running the Suite

1. Start `discovery-server`.
2. Start `inventory`.
3. Start `item`.
4. Start `order`.
5. Call the service endpoints through the individual service ports.

## Notes

- The legacy monolithic CNKart codebase has been replaced by the microservice suite in this version.
- Build artifacts and IDE-specific files are intentionally excluded from the repository.
- Configuration files are retained in each service folder for local setup.

## Suggested Topics

`java`, `spring-boot`, `spring-data-jpa`, `spring-cloud`, `eureka`, `openfeign`, `hystrix`, `mysql`, `microservices`, `ecommerce`, `rest-api`
