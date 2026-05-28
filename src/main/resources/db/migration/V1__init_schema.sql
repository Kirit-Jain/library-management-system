-- Schema

-- Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(15),
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_attempts INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- users to roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- refresh tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PUBLISHERS
CREATE TABLE publishers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(15),
    email VARCHAR(100),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Authors
CREATE TABLE authors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    biography TEXT,
    date_of_birth DATE,
    nationality VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id BIGINT REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books
CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    publisher_id BIGINT NOT NULL REFERENCES publishers(id) ON DELETE CASCADE,
    publication_year INT,
    edition VARCHAR(50),
    language VARCHAR(50),
    cover_image_url VARCHAR(255),
    page_count INT,
    total_copies INT DEFAULT 0,
    available_copies INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books to Authors
CREATE TABLE book_authors (
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

--book to categories
CREATE TABLE book_categories (
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, category_id)
);

-- library branches
CREATE TABLE library_branches (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    address   TEXT NOT NULL,
    phone     VARCHAR(20),
    email     VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- floors
CREATE TABLE floors (
    id           BIGSERIAL PRIMARY KEY,
    branch_id    BIGINT NOT NULL REFERENCES library_branches(id),
    floor_number INT NOT NULL,
    name         VARCHAR(50)
);

-- sections
CREATE TABLE sections (
    id       BIGSERIAL PRIMARY KEY,
    floor_id BIGINT NOT NULL REFERENCES floors(id),
    name     VARCHAR(100) NOT NULL,
    code     VARCHAR(10) UNIQUE NOT NULL
);

-- shelves
CREATE TABLE shelves (
    id         BIGSERIAL PRIMARY KEY,
    section_id BIGINT NOT NULL REFERENCES sections(id),
    shelf_code VARCHAR(20) UNIQUE NOT NULL,
    capacity   INT DEFAULT 100
);

-- book copies
CREATE TABLE book_copies (
    id               BIGSERIAL PRIMARY KEY,
    book_id          BIGINT NOT NULL REFERENCES books(id),
    barcode          VARCHAR(50) UNIQUE NOT NULL,
    shelf_id         BIGINT REFERENCES shelves(id),
    branch_id        BIGINT REFERENCES library_branches(id),
    condition        VARCHAR(20) DEFAULT 'GOOD',
    status           VARCHAR(20) DEFAULT 'AVAILABLE',
    acquisition_date DATE,
    price            DECIMAL(10,2),
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- members
CREATE TABLE members (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT UNIQUE NOT NULL REFERENCES users(id),
    membership_number   VARCHAR(20) UNIQUE NOT NULL,
    membership_type     VARCHAR(20) DEFAULT 'STANDARD',
    max_books_allowed   INT DEFAULT 5,
    max_borrow_days     INT DEFAULT 14,
    membership_start    DATE NOT NULL,
    membership_expiry   DATE NOT NULL,
    total_fines_pending DECIMAL(10,2) DEFAULT 0.00,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- borrow records
CREATE TABLE borrowings (
    id             BIGSERIAL PRIMARY KEY,
    member_id      BIGINT NOT NULL REFERENCES members(id),
    book_copy_id   BIGINT NOT NULL REFERENCES book_copies(id),
    borrow_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date       TIMESTAMP NOT NULL,
    return_date    TIMESTAMP,
    renewed_count  INT DEFAULT 0,
    status         VARCHAR(20) DEFAULT 'ACTIVE',
    issued_by      BIGINT REFERENCES users(id),
    returned_to    BIGINT REFERENCES users(id),
    notes          TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- reservations
CREATE TABLE reservations (
    id               BIGSERIAL PRIMARY KEY,
    member_id        BIGINT NOT NULL REFERENCES members(id),
    book_id          BIGINT NOT NULL REFERENCES books(id),
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date      TIMESTAMP NOT NULL,
    status           VARCHAR(20) DEFAULT 'PENDING',
    notified         BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- fines
CREATE TABLE fines (
    id           BIGSERIAL PRIMARY KEY,
    member_id    BIGINT NOT NULL REFERENCES members(id),
    borrowing_id BIGINT NOT NULL REFERENCES borrowings(id),
    amount       DECIMAL(10,2) NOT NULL,
    fine_type    VARCHAR(30) NOT NULL,
    fine_date    DATE NOT NULL,
    due_date     DATE,
    paid_date    DATE,
    status       VARCHAR(20) DEFAULT 'UNPAID',
    paid_amount  DECIMAL(10,2) DEFAULT 0.00,
    notes        TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- notifications
CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    title      VARCHAR(200) NOT NULL,
    message    TEXT NOT NULL,
    type       VARCHAR(30) NOT NULL,
    is_read    BOOLEAN DEFAULT FALSE,
    sent_via   VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- audit logs
CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users(id),
    action      VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id   BIGINT,
    old_value   TEXT,
    new_value   TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_book_copies_barcode ON book_copies(barcode);
CREATE INDEX idx_book_copies_status ON book_copies(status);
CREATE INDEX idx_book_copies_book_id ON book_copies(book_id);
CREATE INDEX idx_borrowings_member ON borrowings(member_id);
CREATE INDEX idx_borrowings_status ON borrowings(status);
CREATE INDEX idx_borrowings_due_date ON borrowings(due_date);
CREATE INDEX idx_fines_member ON fines(member_id);
CREATE INDEX idx_fines_status ON fines(status);
CREATE INDEX idx_members_number ON members(membership_number);
CREATE INDEX idx_members_user ON members(user_id);