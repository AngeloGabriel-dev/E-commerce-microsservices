CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_price DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS seller_orders (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    seller_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    sub_total DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    seller_order_id UUID NOT NULL REFERENCES seller_orders(id),
    product_id UUID NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);