# Performance Report: REST vs GraphQL

## Smart E-Commerce System — API Performance Comparison

### Methodology

Both REST and GraphQL endpoints were tested for the same query: **Fetch a product by ID with category details**.

- **Tool**: cURL / Postman
- **Environment**: Local development (Spring Boot 3.2.1, PostgreSQL, JDK 21)
- **Iterations**: 10 per endpoint
- **Measurement**: Response time (ms) and payload size (bytes)

---

### Test Case: Fetch Product by ID (with Category)

#### REST Endpoint
```
GET http://localhost:8080/api/products/1
```

#### GraphQL Query
```graphql
query {
  product(id: 1) {
    productId
    name
    description
    price
    stockQuantity
    category {
      categoryId
      name
      description
    }
  }
}
```

---

### Results

#### Response Time (ms)

| Run | REST (ms) | GraphQL (ms) |
|-----|-----------|--------------|
|  1  |    45     |      52      |
|  2  |    12     |      18      |
|  3  |    10     |      15      |
|  4  |    11     |      14      |
|  5  |     9     |      13      |
|  6  |    10     |      14      |
|  7  |    11     |      15      |
|  8  |    10     |      13      |
|  9  |     9     |      12      |
| 10  |    10     |      13      |

| Metric    | REST   | GraphQL |
|-----------|--------|---------|
| Average   | 13.7ms | 17.9ms  |
| Min       |  9ms   | 12ms    |
| Max       | 45ms   | 52ms    |

> **Note**: First request is slower due to JPA lazy initialization and connection pool warm-up.
> Excluding warm-up (runs 2-10), REST averages **10.2ms** and GraphQL averages **14.1ms**.

#### Payload Size

| Metric            | REST     | GraphQL  |
|-------------------|----------|----------|
| Response size     | ~420 bytes | ~280 bytes |
| Overhead fields   | status, message wrapper | None |

---

### Analysis

1. **Response Time**: REST is ~25% faster on average due to simpler request parsing. GraphQL adds overhead for query parsing and validation against the schema.

2. **Payload Size**: GraphQL returns ~33% smaller payloads because clients request only the fields they need. REST returns the full `ApiResponse` wrapper with all product fields.

3. **Flexibility**: GraphQL excels when clients need varying subsets of data. A mobile client can request only `name` and `price`, reducing bandwidth.

4. **Sorting & Filtering Performance**: Both REST and GraphQL use the same `SpringProductService` backed by JPA Specifications. Database-level `ORDER BY` and `WHERE` clauses leverage B-Tree indexes (`idx_products_name`, `idx_products_category`) for O(log n) performance rather than in-memory sorting.

5. **When to use each**:
   - **REST**: Simple CRUD, well-defined responses, caching-friendly (HTTP cache headers)
   - **GraphQL**: Complex queries, mobile clients needing minimal data, aggregated queries

### Conclusion

For simple single-entity lookups, REST offers marginally better performance. GraphQL provides significant advantages in payload optimization and query flexibility, making it ideal for complex front-end requirements where over-fetching is a concern.
