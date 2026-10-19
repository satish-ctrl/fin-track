CREATE UNIQUE INDEX uq_categories_user_name_lower
    ON categories (user_id, LOWER(name));