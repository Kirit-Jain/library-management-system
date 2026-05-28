CREATE TABLE borrow_requests (
    id                 BIGSERIAL PRIMARY KEY,
    member_id          BIGINT NOT NULL REFERENCES members(id),
    book_id            BIGINT NOT NULL REFERENCES books(id),
    status             VARCHAR(20) DEFAULT 'PENDING',
    approved_by        BIGINT REFERENCES users(id),
    approved_at        TIMESTAMP,
    rejection_reason   TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_borrow_requests_member ON borrow_requests(member_id);
CREATE INDEX idx_borrow_requests_status ON borrow_requests(status);