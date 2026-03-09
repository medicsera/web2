CREATE TABLE order_dishes (
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    dish_id BIGINT NOT NULL REFERENCES dishes(id),
    PRIMARY KEY (order_id, dish_id)
);

CREATE INDEX idx_order_dishes_order_id ON order_dishes(order_id);
CREATE INDEX idx_order_dishes_dish_id ON order_dishes(dish_id);