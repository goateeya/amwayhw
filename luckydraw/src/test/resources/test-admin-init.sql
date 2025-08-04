MERGE INTO roles (id, name) KEY(id) VALUES (1, 'ROLE_ADMIN');
MERGE INTO roles (id, name) KEY(id) VALUES (2, 'ROLE_USER');

MERGE INTO users (id, email, password, username) KEY(id) VALUES (
  1,
  'admin@123.com',
  '$2a$10$7bXuneCzXiTVyyDWmPLQDuyr10H9CEV6WX3Bk9Gt8Q9J4Jxn/bM8C',
  'admin'
);


MERGE INTO users (id, email, password, username) KEY(id) VALUES (
  2,
  'min001@123.com',
  '$2a$10$OM9haU8aJBRTnUfng7UQrOD3yMit/yftetGwS0NwQPS0sxrBHnKUe',
  'min001'
);

MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id) VALUES (1, 1);
MERGE INTO user_roles (user_id, role_id) KEY(user_id, role_id) VALUES (2, 2);

-- Reset auto-increment sequence for users table to avoid primary key conflict
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;
