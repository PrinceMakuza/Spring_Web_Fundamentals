-- Smart E-Commerce System Schema
-- Normalized to Third Normal Form (3NF)
-- All tables enforce referential integrity via foreign keys
-- CHECK constraints ensure data validity

-- 1. Categories Table
CREATE TABLE IF NOT EXISTS Categories (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS Users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- 3. Products Table
CREATE TABLE IF NOT EXISTS Products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    category_id INTEGER NOT NULL REFERENCES Categories(category_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES Users(user_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0)
);

-- 5. OrderItems Table
CREATE TABLE IF NOT EXISTS OrderItems (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES Orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    product_id INTEGER NOT NULL REFERENCES Products(product_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0)
);

-- 6. Reviews Table
CREATE TABLE IF NOT EXISTS Reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES Products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    user_id INTEGER NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Inventory Table
CREATE TABLE IF NOT EXISTS Inventory (
    product_id INTEGER PRIMARY KEY REFERENCES Products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    quantity_on_hand INTEGER NOT NULL CHECK (quantity_on_hand >= 0),
    reorder_level INTEGER NOT NULL DEFAULT 10 CHECK (reorder_level >= 0)
);
