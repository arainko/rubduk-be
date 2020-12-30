CREATE TABLE pending_friend_requests
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    requester_id BIGINT NOT NULL,
    requestee_id BIGINT NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (requester_id) REFERENCES users (id),
    FOREIGN KEY (requestee_id) REFERENCES users (id)
);

CREATE TABLE friends
(
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    date_added TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (from_user_id) REFERENCES users (id),
    FOREIGN KEY (to_user_id) REFERENCES users (id)
);