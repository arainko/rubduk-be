CREATE SCHEMA database;

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL,
    name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth DATE NOT NULL,
    created_on DATE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS posts (
    id INT NOT NULL,
    contents TEXT NOT NULL,
    user_id INT NOT NULL,
    date_added DATE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments (
    id INT NOT NULL,
    contents TEXT NOT NULL,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    date_added DATE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
