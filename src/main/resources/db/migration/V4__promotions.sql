/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 * * Migration: V4__promotions
 * Description: Schema for managing discount codes and promotional campaigns.
 * Author: Patrick1986Git
 */

-- Create extension for UUID if not exists (standard in many Postgres environments)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

/**
 * Table: discount_codes
 * Stores information about promotional codes, their validity periods, and usage limits.
 */
CREATE TABLE discount_codes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    
    -- Unique code used by customers at checkout
    code VARCHAR(50) NOT NULL UNIQUE,
    
    -- Percentage value of the discount (e.g., 20 for 20%)
    discount_percent INT NOT NULL CONSTRAINT chk_discount_range CHECK (discount_percent > 0 AND discount_percent <= 100),
    
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    
    -- Maximum number of times this code can be used (NULL means unlimited)
    usage_limit INT,
    
    -- Current number of successful redemptions
    used_count INT DEFAULT 0,
    
    -- Manual toggle for administrative purposes
    active BOOLEAN DEFAULT TRUE,
    
    -- Audit fields for tracking record lifecycle
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    -- Soft delete mechanism
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP
);

-- Add index on code for faster lookup during checkout
CREATE INDEX idx_discount_codes_code ON discount_codes(code) WHERE active = TRUE AND deleted = FALSE;