-- ============================================
-- V4: Library Settings + Enhanced Audit Logs
-- ============================================

-- Library Settings Table
CREATE TABLE library_settings (
    id              BIGSERIAL PRIMARY KEY,
    setting_key     VARCHAR(100) UNIQUE NOT NULL,
    setting_value   VARCHAR(500) NOT NULL,
    description     TEXT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT REFERENCES users(id)
);

-- Default settings
INSERT INTO library_settings (setting_key, setting_value, description) VALUES
('fine_per_day', '10.00', 'Fine amount per day for overdue books in Rupees'),
('max_renewals', '2', 'Maximum number of renewals allowed per borrowing'),
('reservation_expiry_days', '3', 'Days before a reservation expires'),
('max_books_standard', '5', 'Max books for standard members'),
('max_books_premium', '10', 'Max books for premium members'),
('max_books_student', '3', 'Max books for student members'),
('max_books_senior', '7', 'Max books for senior members'),
('borrow_days_standard', '14', 'Standard borrowing period in days'),
('borrow_days_premium', '21', 'Premium borrowing period in days'),
('fine_threshold_block', '100.00', 'Unpaid fine amount that blocks new borrowing'),
('library_open_time', '09:00', 'Library opening time'),
('library_close_time', '18:00', 'Library closing time'),
('email_notifications_enabled', 'true', 'Enable email notifications system-wide');

-- File Uploads Table (track uploaded files)
CREATE TABLE file_uploads (
    id              BIGSERIAL PRIMARY KEY,
    file_name       VARCHAR(255) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_type       VARCHAR(50) NOT NULL,
    file_size       BIGINT NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    uploaded_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_uploads_entity ON file_uploads(entity_type, entity_id);
CREATE INDEX idx_file_uploads_uploaded_by ON file_uploads(uploaded_by);

-- Add profile_image_url to users if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);