-- The user's tasks: what is to be done, how far it has got, and when it was added and last changed.
create table tasks (
    id          char(36)      not null primary key,
    title       varchar(100)  not null,
    description varchar(1000) not null,
    status      varchar(16)   not null,
    created_at  timestamp     not null,
    updated_at  timestamp     not null
);
