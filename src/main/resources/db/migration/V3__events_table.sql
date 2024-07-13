CREATE TABLE events
(
    id      integer primary key,
    user_id int,
    file_id int,
    foreign key (user_id) references users (id),
    foreign key (file_id) references files (id)
);