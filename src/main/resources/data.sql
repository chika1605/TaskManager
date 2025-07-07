INSERT INTO users (id, username, email, password, first_name, last_name, role, created_at, updated_at)
VALUES (1, 'admin', 'admin@example.com', '$2a$10$zMzDZn5W9o7nL/0EyIsHkudcWi6nBbXqSYe8Q9Ek0OgyPV0Uc29Ce', 'System', 'Admin', 'ADMIN', now(), now());

INSERT INTO users (id, username, email, password, first_name, last_name, role, created_at, updated_at)
VALUES (2, 'manager', 'manager@example.com', '$2a$10$UOqNs/txKc6xRKn7HSkvH.NHy9UMp2VpMoUOKpGyi99sDCEq1B5lq', 'Team', 'Manager', 'MANAGER', now(), now());
