-- Indexes for Smart E-Commerce System
-- Optimized for search and joins

-- For fast product searching by name (case-insensitive search often uses name)
CREATE INDEX idx_products_name ON Products(name);

-- For faster filtering and joins by category
CREATE INDEX idx_products_category ON Products(category_id);

-- For fast lookup of orders by user history
CREATE INDEX idx_orders_user ON Orders(user_id);
