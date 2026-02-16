/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 * * Migration: V7__product_images
 * Description: Schema definition for managing product gallery images.
 * Relates to: products table.
 * * This table stores URLs to external storage (e.g., AWS S3, Azure Blob) 
 * and maintains the display order of images for each product.
 */

-- Table for product gallery assets
CREATE TABLE product_images (
    id UUID NOT NULL,
    product_id UUID NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    
    PRIMARY KEY (id),
    
    -- Ensures images are removed when the parent product is deleted
    CONSTRAINT fk_product_images_product 
        FOREIGN KEY (product_id) 
        REFERENCES products (id) 
        ON DELETE CASCADE
);

-- Index for optimized product gallery retrieval
CREATE INDEX idx_product_images_product_id ON product_images(product_id);

-- Database-level documentation for DBAs and Data Analysts
COMMENT ON TABLE product_images IS 'Stores metadata and references to product images in external storage';
COMMENT ON COLUMN product_images.image_url IS 'Full URI or path to the image resource';
COMMENT ON COLUMN product_images.sort_order IS 'Determines the display sequence in the product gallery';