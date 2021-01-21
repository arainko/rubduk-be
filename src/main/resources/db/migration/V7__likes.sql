CREATE TABLE likes
(
    userId BIGINT REFERENCES users(id) NOT NULL,
    postId BIGINT REFERENCES posts(id) NOT NULL
);
