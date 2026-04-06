# Changelog

All notable changes to this project are documented in this file.

## [v2.0.0] - 2026-04-06

### Summary
Second version of the CNKart API that expands the project with query-based filtering for item descriptions, price thresholds, and category ordering.

### Highlights

- Added `GET /api/item/desc/{desc}` for description-prefix item lookup.
- Added `GET /api/details/item/price/{price}` for filtering item details above a price.
- Added `GET /api/details/item/category/{category}` for category-based detail lookup ordered by price.
- Introduced native-query and named-query examples in the entity layer.
- Updated the API route structure to use `/api` and `/api/details` prefixes.

### Notes

This version improves the project’s learning value by combining CRUD flows with multiple JPA query patterns in the same catalog API.

## [v1.0.0] - 2026-04-04

### Summary
Initial publication of the CNKart API as a clean, portfolio-ready Spring Boot REST project.

### Highlights

- Added a recruiter-friendly README with API overview, features, run steps, and project structure.
- Added a changelog for future version tracking.
- Cleaned IDE files and build artifacts before publishing.
- Preserved the original item and item-details management workflow built on Spring Data JPA and MySQL.

### Notes

This version establishes the project as a compact learning showcase for one-to-one relational CRUD management with Spring Boot.
