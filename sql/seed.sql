-- Seed Data for Smart E-Commerce System

-- Categories
INSERT INTO Categories (name, description) VALUES
('Electronics', 'Gadgets and electronic devices'),
('Home & Kitchen', 'Appliances and home decor'),
('Books', 'Physical and electronic books'),
('Clothing', 'Apparel and accessories');

-- Users
INSERT INTO Users (name, email, password) VALUES
('John Doe', 'john@example.com', 'password123'),
('Jane Smith', 'jane@example.com', 'securepass'),
('Admin User', 'admin@ecommerce.com', 'admin789');

-- Products
INSERT INTO Products (name, description, price, category_id) VALUES
('Laptop Pro', 'High-performance laptop for professionals', 1299.99, 1),
('Wireless Mouse', 'Ergonomic wireless mouse with 2.4GHz connection', 25.50, 1),
('Coffee Maker', 'Drip coffee maker with programmable timer', 49.99, 2),
('Standing Desk', 'Adjustable height standing desk', 350.00, 2),
('Java Programming', 'Comprehensive guide to Java 21', 55.00, 3),
('Cotton T-Shirt', 'Breathable cotton t-shirt', 15.00, 4);

-- Inventory
INSERT INTO Inventory (product_id, quantity_on_hand, reorder_level) VALUES
(1, 50, 10),
(2, 200, 20),
(3, 30, 5),
(4, 15, 2),
(5, 100, 15),
(6, 150, 25);

-- Reviews
INSERT INTO Reviews (product_id, user_id, rating, comment) VALUES
(1, 1, 5, 'Great laptop, very fast!'),
(2, 2, 4, 'Good mouse, but the battery life could be better.'),
(3, 1, 3, 'Average coffee maker, does the job.');
