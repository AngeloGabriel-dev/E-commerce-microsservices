CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    category VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    attributes JSONB,
    image_urls JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);