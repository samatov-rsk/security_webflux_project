CREATE TABLE users
(
    id       int generated by default as identity primary key,
    username varchar(255)  not null unique,
    password varchar(2048) not null,
    roles     varchar(32)   not null,
    status   varchar(32)   not null
);
CREATE TABLE files
(
    id        int generated by default as identity primary key,
    name      varchar(255)  not null unique,
    location varchar(2048) not null unique,
    status varchar(255) not null

);
CREATE TABLE events
(
    id      int generated by default as identity primary key,
    user_id int,
    file_id int,
    foreign key (user_id) references users (id),
    foreign key (file_id) references files (id)
);