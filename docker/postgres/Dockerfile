# #############################################################################
# ENTERPRISE SHOP - DATABASE INFRASTRUCTURE
# Custom PostgreSQL image with Polish Full-Text Search (FTS) capabilities.
# Based on lightweight Alpine Linux for optimized security and footprint.
# #############################################################################

FROM postgres:16-alpine

# Metadata for image management and traceability
LABEL maintainer="Your Company Name" \
      module="infrastructure" \
      description="PostgreSQL 16 with Polish linguistic dictionaries"

# Use ARG for maintainability - allows easy path updates in the future
# Note: Standard path for tsearch_data in Alpine-based Postgres
ARG TSEARCH_DIR=/usr/local/share/postgresql/tsearch_data

/**
 * Copy Polish Hunspell dictionaries to the PostgreSQL search data directory.
 * These files are required for the 'polish_hunspell' dictionary 
 * defined in Flyway migration V5.
 */
COPY tsearch_data/polish.dict ${TSEARCH_DIR}/
COPY tsearch_data/polish.affix ${TSEARCH_DIR}/
COPY tsearch_data/polish.stop ${TSEARCH_DIR}/

# Ensure correct file permissions for the postgres engine
# 644 = Owner (read/write), Group (read), Others (read)
RUN chmod 644 ${TSEARCH_DIR}/polish.*

/**
 * PostgreSQL on Alpine already includes 'unaccent' and other contrib modules.
 * No additional installation steps are required for extensions.
 */