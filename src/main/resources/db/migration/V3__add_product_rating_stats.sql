ALTER TABLE products ADD COLUMN average_rating NUMERIC(3,2) NOT NULL DEFAULT 0.0;
ALTER TABLE products ADD COLUMN review_count INT NOT NULL DEFAULT 0;

-- Indeks wydajnościowy dla sortowania po ocenach
CREATE INDEX idx_products_average_rating ON products(average_rating DESC);