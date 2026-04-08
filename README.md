# Smart E-Commerce System

A JavaFX application with a PostgreSQL database, implementing product management, advanced search, in-memory caching, and performance optimization. Built with a layered architecture (Controller → Service → DAO) using JDBC with parameterized queries.

---

## 🚀 Features

- **Dark Mode Professional UI**: Modern dark color scheme with sidebar navigation
- **Admin Panel**: Full CRUD operations for products and categories with input validation and confirmation dialogs
- **Product Browsing**: Dynamic product listings with pagination (10 per page), search, and filtering
- **Search & Filtering**: Case-insensitive name search (ILIKE) and category-based filtering
- **Optimization Layer**:
    - **In-Memory Caching**: Results stored in a `HashMap` for fast lookup (O(1) response)
    - **Custom Sorting**: Java Comparator-based sorting for name, price, and stock
    - **Database Indexing**: Indexes on `Products.name`, `Products.category_id`, and `Orders.user_id`
- **Performance Reports**: Detailed analysis of query optimizations (before vs. after)
- **NoSQL Design**: JSON-based schema design for unstructured customer feedback (reviews/logs)
- **Report Generation**: Auto-generated `validation_report.txt` and `performance_report.txt`
- **Role-Based Navigation**: User and Admin sections with different views

---

## 📂 Database Models & ERD

### Conceptual Model

The system models an e-commerce domain with the following entities and relationships:

- **Users** place **Orders**
- **Orders** contain multiple **OrderItems**
- **OrderItems** reference **Products**
- **Products** belong to **Categories**
- **Products** have **Reviews** from **Users**
- **Inventory** tracks stock for each **Product**

```
Users ──── 1:N ──── Orders
                      │
                    1:N
                      │
                  OrderItems ──── N:1 ──── Products ──── N:1 ──── Categories
                                              │                       
                                            1:N                     
                                              │                     
                                           Reviews ──── N:1 ──── Users
                                              │
                                           Inventory (1:1 with Products)
```

### Logical Model (3NF — Third Normal Form)

All tables are in Third Normal Form:
- Every non-key attribute depends on the whole key (2NF)
- No transitive dependencies exist (3NF)

| Table | Attributes |
|-------|-----------|
| **Users** | user_id (PK), name, email (UNIQUE), password |
| **Categories** | category_id (PK), name (UNIQUE), description |
| **Products** | product_id (PK), name (UNIQUE), description, price, category_id (FK → Categories) |
| **Orders** | order_id (PK), user_id (FK → Users), order_date, total_amount |
| **OrderItems** | order_item_id (PK), order_id (FK → Orders), product_id (FK → Products), quantity, unit_price |
| **Reviews** | review_id (PK), product_id (FK → Products), user_id (FK → Users), rating (1-5), comment, review_date |
| **Inventory** | product_id (PK, FK → Products), quantity_on_hand, reorder_level |

### Physical Model

| SQL Type | Used For |
|----------|---------|
| `SERIAL` | Auto-incrementing primary keys |
| `VARCHAR(100)` | name, email, password |
| `DECIMAL(10,2)` | price, total_amount, unit_price |
| `TIMESTAMP` | order_date, review_date |
| `INTEGER` | quantity, rating, stock levels |
| `TEXT` | description, comment |

**Constraints Applied:**
- `PRIMARY KEY` on all tables
- `FOREIGN KEY` with `ON DELETE RESTRICT`, `ON UPDATE CASCADE` (prevents orphaned records)
- `NOT NULL` on required fields
- `UNIQUE` on product name, category name, user email
- `CHECK` constraints: `price >= 0`, `quantity > 0`, `rating BETWEEN 1 AND 5`

---

## 🛠️ Prerequisites

- **Java**: 17 or higher (Java 21 recommended)
- **PostgreSQL**: 13 or higher
- **Maven**: 3.6 or higher

---

## 📂 Database Setup

1. **Create the database**:
   ```bash
   createdb -U postgres ecommerce_db
   ```

2. **Execute the SQL scripts** (in order):
   ```bash
   psql -U postgres -d ecommerce_db -f sql/schema.sql
   psql -U postgres -d ecommerce_db -f sql/seed.sql
   psql -U postgres -d ecommerce_db -f sql/indexes.sql
   ```

---

## ⚙️ Configuration

Edit `src/main/java/com/ecommerce/config/db.properties` with your PostgreSQL credentials:

```properties
db.url=jdbc:postgresql://localhost:5432/ecommerce_db
db.username=your_username
db.password=your_password
db.poolSize=10
```

---

## 🖥️ How to Run

Compile and launch the application using Maven:

```bash
mvn clean compile javafx:run
```

---

## 📊 Application Architecture

```
Controller Layer (JavaFX UI)
    │
    ├── ProductController    → Product browsing, search, pagination
    └── AdminController      → CRUD for products and categories
        │
Service Layer (Business Logic)
    │
    ├── ProductService       → Cache integration, sorting, business rules
    └── CacheService         → HashMap-based in-memory cache
        │
DAO Layer (Data Access)
    │
    ├── ProductDAO           → Parameterized SQL queries
    ├── CategoryDAO          → Category CRUD operations
    └── DatabaseConnection   → HikariCP connection pool
```

---

## 📈 Performance & Data Structures

### In-Memory Cache (HashMap)
- **Data Structure**: `HashMap<String, List<Product>>`
- **Lookup Time**: O(1) average (mirrors database hash index)
- **Key Format**: `"search:{term}:category:{id}:page:{n}:sort:{field}"`
- **Invalidation**: Full cache clear after any add/update/delete operation

### Sorting (Comparator)
- **Algorithm**: Java's `Collections.sort()` with `Comparator`
- **Options**: Name (A-Z), Price (Low→High), Price (High→Low)
- **Mirrors**: B-Tree index behavior for maintaining ordered data

### Database Indexes
- `idx_products_name` — Accelerates name-based search
- `idx_products_category_id` — Speeds up category filtering and JOINs
- `idx_orders_user_id` — Optimizes user order history lookups

---

## 📋 Reports

| Report | Description |
|--------|-------------|
| [performance_report.txt](performance_report.txt) | Query optimization analysis (before vs. after indexes + caching) |
| [validation_report.txt](validation_report.txt) | Automated test results for CRUD, search, cache, and constraints |
| [nosql_design.txt](nosql_design.txt) | NoSQL document schema for reviews and activity logs |

### Generating Reports

From the application UI:
1. Navigate to **📋 Generate Reports** in the sidebar
2. Click **Generate validation_report.txt** or **Generate performance_report.txt**

Reports are written to the project root directory.

---

## 📁 Project Structure

```
SmartECommerceSystem/
├── pom.xml                          Maven build configuration
├── README.md                        This file
├── sql/
│   ├── schema.sql                   3NF table definitions with constraints
│   ├── seed.sql                     Sample data (categories, products, orders, reviews)
│   └── indexes.sql                  Performance indexes
├── performance_report.txt           Query performance analysis
├── validation_report.txt            Test evidence report
├── nosql_design.txt                 NoSQL schema design for reviews/logs
└── src/main/
    ├── java/com/ecommerce/
    │   ├── MainApp.java             Application entry point + sidebar layout
    │   ├── controller/
    │   │   ├── ProductController.java   Browse, search, pagination
    │   │   └── AdminController.java     CRUD for products & categories
    │   ├── service/
    │   │   ├── ProductService.java      Business logic + cache integration
    │   │   └── CacheService.java        HashMap cache with invalidation
    │   ├── dao/
    │   │   ├── ProductDAO.java          Parameterized queries
    │   │   ├── CategoryDAO.java         Category data access
    │   │   └── DatabaseConnection.java  HikariCP connection pool
    │   ├── model/
    │   │   ├── Product.java             Product entity
    │   │   └── Category.java            Category entity
    │   ├── util/
    │   │   ├── ReportGenerator.java     Auto-generates reports
    │   │   └── PerformanceMonitor.java  Query timing measurement
    │   └── config/
    │       └── db.properties            Database credentials
    └── resources/
        └── css/
            └── styles.css               Dark mode theme
```
