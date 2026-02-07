/*
 * Copyright (c) 2026 Your Company Name. All rights reserved.
 * * Migration: V5__full_text_search
 * Description: Implementation of advanced Full Text Search (FTS) for products.
 * Includes Polish language stemming, unaccent support, and GIN indexing.
 * Author: Patrick1986Git
 */

-- Enable 'unaccent' extension to support search without Polish diacritics (e.g., 'zolty' matches 'żółty')
CREATE EXTENSION IF NOT EXISTS unaccent;

/**
 * Polish Hunspell Dictionary Configuration
 * Note: Ensure that polish.dict and polish.affix files are present in the PostgreSQL shared directory.
 */
CREATE TEXT SEARCH DICTIONARY polish_hunspell (
    TEMPLATE = ispell,
    DictFile = polish,
    AffFile = polish,
    StopWords = polish
);

-- Define a custom Polish text search configuration based on a simple template
CREATE TEXT SEARCH CONFIGURATION public.polish (COPY = pg_catalog.simple);

-- Configure token mapping: unaccent -> hunspell (stemming) -> simple (fallback)
ALTER TEXT SEARCH CONFIGURATION public.polish
    ALTER MAPPING FOR asciiword, asciihword, hword_asciipart,
                  word, hword, hword_part
    WITH unaccent, polish_hunspell, pg_catalog.simple;

/**
 * Update 'products' table to support fast full-text searching.
 * We use a generated tsvector column for maximum performance.
 * Weights: 
 * 'A' - Product Name (highest priority)
 * 'B' - Product Description (secondary priority)
 */
ALTER TABLE products 
ADD COLUMN search_vector tsvector 
GENERATED ALWAYS AS (
    setweight(to_tsvector('public.polish', name), 'A') || 
    setweight(to_tsvector('public.polish', COALESCE(description, '')), 'B')
) STORED;

-- Create Generalized Inverted Index (GIN) for high-speed text search
CREATE INDEX idx_products_search_vector ON products USING GIN(search_vector);