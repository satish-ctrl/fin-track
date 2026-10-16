
-- Remove the existing unconditional unique constraints.
ALTER TABLE users
DROP CONSTRAINT users_email_key;

ALTER TABLE users
DROP CONSTRAINT users_username_key;

-- Active users must have unique emails.
CREATE UNIQUE INDEX users_email_active_unique
    ON users (email)
    WHERE status = 'ACTIVE';

-- Active users must have unique usernames.
CREATE UNIQUE INDEX users_username_active_unique
    ON users (username)
    WHERE status = 'ACTIVE';
