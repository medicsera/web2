create  table restaurants(
    id BIGSERIAL  primary key,
    name varchar(255) not null ,
    address text not null ,
    created_at timestamp default current_timestamp
);