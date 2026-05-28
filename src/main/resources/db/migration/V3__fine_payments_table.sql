-- Fine Payment Table
CREATE TABLE fine_payments (
    id BIGSERIAL PRIMARY KEY,
    fine_id BIGINT NOT NULL REFERENCES fines(id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(100),
    recived_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fine_payments_fine ON fine_payments(fine_id);