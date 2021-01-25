alter table comments
    drop constraint comments_post_id_fkey,
    add constraint comments_post_id_fkey
        foreign key (post_id)
            references posts (id)
            on delete cascade;

alter table comments
    drop constraint comments_user_id_fkey,
    add constraint comments_user_id_fkey
        foreign key (user_id)
            references users (id)
            on delete cascade;

alter table posts
    drop constraint posts_user_id_fkey,
    add constraint posts_user_id_fkey
        foreign key (user_id)
            references users (id)
            on delete cascade;

alter table media
    drop constraint media_user_id_fkey,
    add constraint media_user_id_fkey
        foreign key (user_id)
            references users (id)
            on delete cascade;

alter table friend_requests
    drop constraint friend_requests_from_user_id_fkey,
    add constraint friend_requests_from_user_id_fkey
        foreign key (from_user_id)
        references users (id)
        on delete cascade;

alter table friend_requests
    drop constraint friend_requests_to_user_id_fkey,
    add constraint friend_requests_to_user_id_fkey
        foreign key (to_user_id)
            references users (id)
            on delete cascade;

alter table likes
    drop constraint likes_userid_fkey,
    add constraint likes_userid_fkey
        foreign key (userid)
        references users (id)
        on delete cascade;

alter table likes
    drop constraint likes_postid_fkey,
    add constraint likes_postid_fkey
        foreign key (postid)
            references posts (id)
            on delete cascade;