# CNKart API

Spring Boot REST API for managing catalog items and item details with JPA, MySQL persistence, query-based filtering, and layered controller-service-repository design.

## Overview

This project demonstrates a compact Spring Boot API for managing product-like catalog items. It models each item with linked detail data such as brand, price, and category, and extends the original CRUD-style workflow with named-query and native-query based filtering endpoints for stronger catalog-search learning value.

## Concepts and Features Covered

- Spring Boot REST API setup
- Spring Data JPA repository pattern
- MySQL-backed persistence
- One-to-one relationship between `Item` and `ItemDetails`
- `GET` endpoint for retrieving an item by ID
- `GET` endpoint for listing all items
- `POST` endpoint for saving an item with details
- `PUT` endpoint for updating an item
- `DELETE` endpoint for deleting an item by ID
- `DELETE` endpoint for deleting item details by ID
- Native query based item search by description prefix
- Derived query for item details above a given price
- Named query for item details by category ordered by price

## Tech Stack

- Java 8
- Spring Boot 2.7
- Spring Web
- Spring Data JPA
- MySQL
- Maven
- JUnit 5

## Project Structure

```text
cnkart/
├── CHANGELOG.md
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/cn/ecommerce/
    │   │   ├── controller/
    │   │   ├── dao/
    │   │   ├── entity/
    │   │   ├── service/
    │   │   └── EcommerceApplication.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/cn/ecommerce/
            └── EcommerceApplicationTests.java
```

## How to Run

1. Open a terminal in the project root.
2. Update the MySQL connection values in `src/main/resources/application.yml` if needed.
3. Run `mvn test`.
4. Run `mvn spring-boot:run`.
5. Use the API under `http://localhost:8080`.

Available endpoints:

- `GET /api/item/{id}`
- `GET /api/item`
- `POST /api/item/save`
- `PUT /api/item/update`
- `DELETE /api/item/{id}`
- `GET /api/item/desc/{desc}`
- `DELETE /api/details/item/{id}`
- `GET /api/details/item/price/{price}`
- `GET /api/details/item/category/{category}`

Example request body:

```json
{
  "name": "Wireless Mouse",
  "description": "Compact Bluetooth mouse",
  "itemDetails": {
    "brand": "LogiTech",
    "price": 1299.0,
    "category": "Accessories"
  }
}
```

## Learning Highlights

- Demonstrates how to model one-to-one entity relationships with JPA
- Shows a clean separation between controllers, services, and repository access
- Adds native-query, named-query, and derived-query examples in one compact project
- Keeps the API surface compact while still covering create, read, update, delete, and filter flows
- Works well as a starter project for relational CRUD APIs with Spring Boot

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for managing catalog items and item details with JPA, MySQL persistence, plus native-query and named-query catalog filtering.`
- Suggested topics: `java`, `spring-boot`, `spring-data-jpa`, `mysql`, `rest-api`, `crud-api`, `ecommerce`, `catalog-management`, `native-query`, `named-query`, `maven`, `learning-project`, `portfolio-project`
