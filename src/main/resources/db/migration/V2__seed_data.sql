-- =============================================
-- V2: Seed Initial Data
-- =============================================

-- Roles
INSERT INTO roles (name) VALUES
    ('ADMIN'),
    ('LIBRARIAN'),
    ('MEMBER');

-- Categories
INSERT INTO categories (name, description) VALUES
    ('Fiction', 'Fictional literature'),
    ('Non-Fiction', 'Non-fictional books'),
    ('Science', 'Science related books'),
    ('Technology', 'Technology and computing'),
    ('History', 'Historical books'),
    ('Biography', 'Biographies and autobiographies'),
    ('Children', 'Books for children'),
    ('Reference', 'Reference materials');

-- Default Library Branch
INSERT INTO library_branches (name, address, phone, email) VALUES
    ('Main Branch', '123 Library Street, City', '555-0100', 'main@library.com');

-- Floors
INSERT INTO floors (branch_id, floor_number, name) VALUES
    (1, 0, 'Ground Floor'),
    (1, 1, 'First Floor'),
    (1, 2, 'Second Floor');

-- Sections
INSERT INTO sections (floor_id, name, code) VALUES
    (1, 'Fiction', 'FIC'),
    (1, 'Children', 'CHI'),
    (2, 'Science', 'SCI'),
    (2, 'Technology', 'TEC'),
    (3, 'Reference', 'REF'),
    (3, 'History', 'HIS');

-- Shelves
INSERT INTO shelves (section_id, shelf_code, capacity) VALUES
    (1, 'FIC-A1', 100), (1, 'FIC-A2', 100), (1, 'FIC-B1', 100),
    (2, 'CHI-A1', 80),  (2, 'CHI-A2', 80),
    (3, 'SCI-A1', 100), (3, 'SCI-B1', 100),
    (4, 'TEC-A1', 100), (4, 'TEC-B1', 100),
    (5, 'REF-A1', 60),
    (6, 'HIS-A1', 100);

-- Default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, first_name, last_name, is_active)
VALUES ('admin', 'admin@library.com', '$2a$10$...hashed...', 'Library', 'Admin', true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN';