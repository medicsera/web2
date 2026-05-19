CREATE TABLE dishes
(
    id BIGSERIAL primary key,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    is_available BOOLEAN NOT NULL default true,
    restaurant_id BIGINT references restaurants(id),
    created_at timestamp default current_timestamp
);