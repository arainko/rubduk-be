ALTER TABLE users
    ADD COLUMN profile_pic_id BIGINT REFERENCES media(id);
