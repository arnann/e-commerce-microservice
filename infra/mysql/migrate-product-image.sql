USE product_db;

SET @has_image_url := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'product_db'
      AND table_name = 'product_spu'
      AND column_name = 'image_url'
);

SET @ddl := IF(
    @has_image_url = 0,
    'ALTER TABLE product_spu ADD COLUMN image_url VARCHAR(255) NULL AFTER description',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE product_spu
SET image_url = CASE id
    WHEN 101 THEN 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80'
    WHEN 102 THEN 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=900&q=80'
    WHEN 103 THEN 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=900&q=80'
    WHEN 104 THEN 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=900&q=80'
    WHEN 105 THEN 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=900&q=80'
    ELSE image_url
END
WHERE id IN (101, 102, 103, 104, 105);
