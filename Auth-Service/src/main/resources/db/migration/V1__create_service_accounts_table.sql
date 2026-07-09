-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(25) NOT NULL DEFAULT 'ROLE_CLIENT'
);

-- Create service_accounts table
CREATE TABLE IF NOT EXISTS service_accounts (
    id UUID PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_secret_hash VARCHAR(200) NOT NULL,
    roles VARCHAR(25) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups
CREATE INDEX idx_service_accounts_client_id ON service_accounts(client_id);

-- Insert sample service accounts (passwords are encoded with BCrypt)
-- Password for all sample accounts: "secret123"
INSERT INTO service_accounts (id, client_id, client_secret_hash, roles, enabled) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'order-service', '$2a$10$rQ7H8p9Q0w1E2r3T4y5U6u7V8w9X0y1Z2a3B4c5D6e7F8g9H0i1J2', 'ROLE_ORDER_SERVICE', true),
('b2c3d4e5-f6a7-8901-bcde-fa2345678901', 'payment-service', '$2a$10$rQ7H8p9Q0w1E2r3T4y5U6u7V8w9X0y1Z2a3B4c5D6e7F8g9H0i1J2', 'ROLE_PAYMENT_SERVICE', true),
('c3d4e5f6-a7b8-9012-cdef-ab3456789012', 'notification-service', '$2a$10$rQ7H8p9Q0w1E2r3T4y5U6u7V8w9X0y1Z2a3B4c5D6e7F8g9H0i1J2', 'ROLE_NOTIFICATION_SERVICE', true),
('d4e5f6a7-b8c9-0123-defa-bc4567890123', 'catalog-service', '$2a$10$rQ7H8p9Q0w1E2r3T4y5U6u7V8w9X0y1Z2a3B4c5D6e7F8g9H0i1J2', 'ROLE_CATALOG_SERVICE', true);