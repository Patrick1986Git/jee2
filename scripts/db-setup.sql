-- Bootstrap for a fresh local PostgreSQL instance.
-- Creates only local development role/database (`shop_dev`, `enterprise_shop_dev`).
-- Application schema/tables are created by Flyway migrations at app startup.
-- Run as a PostgreSQL superuser (for example: postgres).
-- Not idempotent: rerunning can fail if role/database already exists.

CREATE USER shop_dev WITH ENCRYPTED PASSWORD 'shop_dev';
CREATE DATABASE enterprise_shop_dev OWNER shop_dev;
GRANT ALL PRIVILEGES ON DATABASE enterprise_shop_dev TO shop_dev;
