CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    name TEXT NOT NULL,
    last_name TEXT,
    date_of_birth DATE,
    created_on TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE posts (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    contents TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    contents TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (post_id) REFERENCES posts (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
