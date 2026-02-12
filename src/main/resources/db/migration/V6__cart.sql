/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 * * Migration: V6__cart
 * Description: Schema definition for Shopping Cart and Cart Items.
 * Dependencies: Users, Products tables.
 * Author: Patrick1986Git
 */

-- Table for storing user shopping carts (One-to-One relationship with Users)
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for individual items within a cart
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES products(id),
    -- Prevent duplicate products in the same cart
    CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

-- Performance: Indexes for frequent foreign key lookups
CREATE INDEX idx_cart_user ON carts(user_id);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- Documentation: Comments on tables and columns for Database Administrators
COMMENT ON TABLE carts IS 'Stores header information for a user shopping cart';
COMMENT ON TABLE cart_items IS 'Stores line items for each shopping cart';