ALTER TABLE users
    ADD COLUMN email TEXT UNIQUE NOT NULL;

CREATE INDEX user_email_index ON users(email);