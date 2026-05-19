create table  orders(
    id BIGSERIAL primary key,
    status varchar(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'DELIVERED', 'CANCELLED')) ,
    user_id bigint not null references users(id),
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp
);