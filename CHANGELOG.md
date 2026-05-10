# Changelog

All notable changes to this project are documented in this file.

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
