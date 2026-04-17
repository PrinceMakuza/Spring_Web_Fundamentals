# NoSQL Design & Migration Report: Smart E-Commerce System

## Executive Summary
This report outlines the strategy for migrating the Smart E-Commerce System from its current PostgreSQL-based relational architecture to a hybrid NoSQL architecture using **MongoDB** for flexible document storage and **Redis** for high-performance caching and state management.

## 1. Architectural Drivers
The migration is driven by the need for:
*   **Horizontal Scalability**: Distributing data across multiple nodes (Sharding).
*   **Schema Flexibility**: Efficiently handling diverse product attributes and dynamic metadata (e.g., varying specs for different product categories).
*   **Performance**: Reducing latency for frequent read operations (User Profile, Product Catalog).

## 2. Document Model (MongoDB)

### 2.1 Products Collection
Instead of normalized tables, products will be stored as rich documents:
```json
{
  "_id": "ObjectId",
  "name": "Validation Test Laptop",
  "description": "High performance computing",
  "price": 999.99,
  "stock_quantity": 10,
  "category": {
    "name": "Laptops",
    "path": "Electronics > Computers"
  },
  "attributes": [
    {"key": "RAM", "value": "16GB"},
    {"key": "CPU", "value": "M2 Pro"}
  ],
  "reviews": [
    {"user": "Alice", "rating": 5, "comment": "Great!"}
  ]
}
```
*   **Benefit**: Single read operation to fetch product details and reviews.

### 2.2 Orders Collection
```json
{
  "_id": "OrderId",
  "user_id": 123,
  "order_date": "2026-04-16T20:45:00Z",
  "total_amount": 1045.50,
  "items": [
    {"product_id": "P1", "name": "Laptop", "qty": 1, "price": 999.99}
  ],
  "status": "COMPLETED"
}
```
*   **Benefit**: Atomic storage of snapshots for historical data integrity.

## 3. High-Performance Caching (Redis)
Redis will replace the custom `CacheService` to handle:
*   **Product Search Results**: Storing serialized JSON of popular search queries with TTL.
*   **User Sessions**: Storing `UserContext` with distributed session support.
*   **Inventory Counters**: Utilizing Redis Atomic operations for high-concurrency checkout.

## 4. Migration Strategy
1.  **Phase 1 (Hybrid)**: Introduce Mongo for Product metadata while keeping Orders in Postgres for ACID transactions.
2.  **Phase 2 (Shadow Write)**: Simultaneously write to both DBs to verify data consistency.
3.  **Phase 3 (Cutover)**: Switch read traffic to NoSQL after performance validation.

## 5. Conclusion
A hybrid approach (Relational for Finance/Orders, NoSQL for Catalog/Engagement) offers the best balance of data integrity and performance for a modern e-commerce platform.
