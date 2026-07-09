CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    client_id UUID NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL,
    mp_payment_id BIGINT UNIQUE,
    mp_preference_id VARCHAR(255),
    mp_status VARCHAR(30),
    mp_init_point VARCHAR(255),
    mp_sandbox_init_point VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);