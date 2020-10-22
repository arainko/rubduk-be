CREATE TABLE users (
    id GENERATED ALWAYS AS IDENTITY BIGINT NOT NULL,
    name TEXT NOT NULL,
    last_name TEXT,
    date_of_birth DATE,
    created_on TIMESTAMPZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE posts (
    id GENERATED ALWAYS AS IDENTITY BIGINT NOT NULL,
    contents TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    date_added TIMESTAMPZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE comments (
    id GENERATED ALWAYS AS IDENTITY BIGINT NOT NULL,
    contents TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    date_added TIMESTAMPZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
