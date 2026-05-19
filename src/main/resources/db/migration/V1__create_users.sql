CREATE table users (
    id BIGSERIAL primary key,
    email varchar(255) not null unique,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    is_active BOOLEAN not null default true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);