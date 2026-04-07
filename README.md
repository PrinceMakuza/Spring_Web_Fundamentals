# Smart E-Commerce System

A JavaFX application with a PostgreSQL database, implementing product management, advanced search, in-memory caching, and performance optimization.

## 🚀 Features

- **Admin Panel**: Full CRUD operations for products and categories with input validation and confirmation dialogs.
- **Product Browsing**: Dynamic product listings with pagination (10 per page), search, and filtering.
- **Search & Filtering**: Case-insensitive name search and category-based filtering.
- **Optimization Layer**: 
    - **In-Memory Caching**: Results stored in a `HashMap` for fast lookup ($O(1)$ response).
    - **Custom Sorting**: Java sorting algorithms for name, price, and stock.
    - **Database Indexing**: Indexes on `Products.name`, `Products.category_id`, and `Orders.user_id` for high performance.
- **Performance Reports**: Detailed analysis of query optimizations (before vs. after).
- **NoSQL Design**: JSON-based schema design for unstructured customer feedback (reviews).

## 🛠️ Prerequisites

- **Java**: 17 or higher (Java 21 recommended)
- **PostgreSQL**: 13 or higher
- **Maven**: 3.6 or higher

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

## ⚙️ Configuration

Before running the application, edit `src/main/java/com/ecommerce/config/db.properties` with your PostgreSQL credentials:

```properties
db.url=jdbc:postgresql://localhost:5432/ecommerce_db
db.username=your_username
db.password=your_password
```

## 🖥️ How to Run

Compile and launch the application using Maven:

```bash
mvn clean compile javafx:run
```

## 📊 Database Models & ERD

### Conceptual Model
- **Entities**: Users, Products, Categories, Orders, OrderItems, Reviews, Inventory.
- **Relationships**:
  - Users place Orders.
  - Orders contain OrderItems.
  - OrderItems reference Products.
  - Products belong to Categories.
  - Products have Reviews and Inventory tracks Products.

### Logical Model (3NF)
- **Users**: user_id (PK), name, email, password.
- **Products**: product_id (PK), name, description, price, category_id (FK).
- **Categories**: category_id (PK), name, description.
- **Orders**: order_id (PK), user_id (FK), order_date, total_amount.
- **OrderItems**: order_item_id (PK), order_id (FK), product_id (FK), quantity, unit_price.
- **Reviews**: review_id (PK), product_id (FK), user_id (FK), rating, comment, date.
- **Inventory**: product_id (PK/FK), quantity_on_hand, reorder_level.

### Physical Model
- **serial/int**: user_id, product_id, category_id, order_id.
- **varchar(100)**: name, email, password.
- **decimal(10,2)**: price, total_amount, unit_price.
- **timestamp**: order_date, date.
- **Constraints**: NOT NULL, UNIQUE, CHECK (price >= 0), ON DELETE RESTRICT, ON UPDATE CASCADE.

## 📈 Performance & Validation

- Check [performance_report.txt](performance_report.txt) for query optimization analysis.
- Check [validation_report.txt](validation_report.txt) for functional test details.
- Check [nosql_design.txt](nosql_design.txt) for unstructured data modeling.
