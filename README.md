# CNKart API

Spring Boot REST API for managing catalog items and item details with JPA, MySQL persistence, and layered controller-service-repository design.

## Overview

This project demonstrates a compact Spring Boot API for managing product-like catalog items. It models each item with linked detail data such as brand, price, and category, making it a useful learning project for understanding one-to-one JPA relationships and CRUD-style REST workflows backed by MySQL.

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
    │   ├── java/com/cn/cnkart/
    │   │   ├── controller/
    │   │   ├── dal/
    │   │   ├── entity/
    │   │   ├── service/
    │   │   └── CnkartApplication.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/cn/cnkart/
            └── CnkartApplicationTests.java
```

## How to Run

1. Open a terminal in the project root.
2. Update the MySQL connection values in `src/main/resources/application.yml` if needed.
3. Run `mvn test`.
4. Run `mvn spring-boot:run`.
5. Use the API under `http://localhost:8080`.

Available endpoints:

- `GET /item/id/{id}`
- `GET /item/all`
- `POST /item/save`
- `PUT /item/update`
- `DELETE /item/delete/id/{id}`
- `DELETE /details/id/{id}`

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
- Keeps the API surface compact while still covering create, read, update, and delete flows
- Works well as a starter project for relational CRUD APIs with Spring Boot

## GitHub Metadata

- Suggested repository description: `Spring Boot REST API for managing catalog items and item details with JPA, MySQL persistence, and layered service design.`
- Suggested topics: `java`, `spring-boot`, `spring-data-jpa`, `mysql`, `rest-api`, `crud-api`, `ecommerce`, `catalog-management`, `maven`, `learning-project`, `portfolio-project`
