UPDATE dishes SET description = '' WHERE description IS NULL;
ALTER TABLE dishes ALTER COLUMN description SET NOT NULL;
ALTER TABLE dishes ALTER COLUMN description SET DEFAULT '';

ALTER TABLE dishes ALTER COLUMN restaurant_id SET NOT NULL;

UPDATE orders SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
ALTER TABLE orders ALTER COLUMN created_at SET NOT NULL;
