-- Indexes for Smart E-Commerce System
-- Optimized for search and joins

-- For fast product searching by name (case-insensitive search often uses name)
CREATE INDEX IF NOT EXISTS idx_products_name ON Products(name);

-- For faster filtering and joins by category
CREATE INDEX IF NOT EXISTS idx_products_category ON Products(category_id);

-- For fast lookup of orders by user history
CREATE INDEX IF NOT EXISTS idx_orders_user ON Orders(user_id);

-- For fast cart item lookups
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON CartItems(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON CartItems(product_id);
