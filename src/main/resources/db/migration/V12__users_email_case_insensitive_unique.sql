CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower
    ON users (lower(email));
