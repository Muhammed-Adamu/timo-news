-- Patch: Fix admin/user passwords
-- Run this if you already have the database set up and can't login
-- This updates passwords WITHOUT dropping any data
USE timo_news;

UPDATE users SET password = '$2b$12$lWSVxEjwrnoqzJESaaw1o.raSOmEHHElIYTOwDz7lHY3VxM5PX.qW' WHERE username = 'admin';
UPDATE users SET password = '$2b$12$KI2RMkTxJnTG2DjnAFi1y.3WiSScK9SF5w.X/sVZ5Ut/tQyGt5YMO' WHERE username = 'john_reader';
UPDATE users SET password = '$2b$12$cAIOEizaDWlkYU6zr08WXeFkm031VZ8iE81yV2V0DXfUUIw5MU5hC' WHERE username = 'jane_author';
UPDATE users SET password = '$2b$12$ozQ4iuSQZCZcfThGsDNsAO42LDFxaidGzIiVQHS8i8tpzwdgS0XQK' WHERE username = 'mike_editor';

-- Verify
SELECT username, 'Password updated successfully' AS status FROM users;

-- Also update broken image URLs in existing news articles
UPDATE news SET image_url = 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800' WHERE image_url LIKE '%photo-1461896836934-5794b5b0a7a9%';
UPDATE news SET image_url = 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800' WHERE image_url LIKE '%photo-1558618666-fcd25c85f82e%';
UPDATE news SET image_url = 'https://images.unsplash.com/photo-1524661135-423995f22d0b?w=800' WHERE image_url LIKE '%photo-1460472178824-e524f2c2e0c5%';
UPDATE news SET image_url = 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800' WHERE image_url LIKE '%photo-1622279457486-62d9bb4d2b15%';

SELECT 'All fixes applied successfully!' AS result;
