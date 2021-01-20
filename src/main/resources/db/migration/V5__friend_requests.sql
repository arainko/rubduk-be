CREATE TABLE friend_requests
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    status TEXT NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (from_user_id) REFERENCES users (id),
    FOREIGN KEY (to_user_id) REFERENCES users (id)
);