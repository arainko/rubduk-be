CREATE TABLE media
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    link       TEXT        NOT NULL,
    user_id    BIGINT      NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);
