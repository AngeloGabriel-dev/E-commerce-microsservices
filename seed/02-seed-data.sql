-- ============================================================================
-- SEED DATA SCRIPT
-- ============================================================================
-- This script runs only on the FIRST initialization of the PostgreSQL container.
-- It creates the necessary tables (since services haven't started yet) and
-- populates them with sample data for testing and demonstration.
-- ============================================================================

-- ============================================================================
-- 1. AUTH-SERVICE DATABASE
-- ============================================================================
\c authservice

-- Create users table (mirrors Auth-Service JPA entity)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(25) NOT NULL DEFAULT 'ROLE_CLIENT'
);

-- Clean existing data (idempotent re-run safety)
DELETE FROM users;

-- BCrypt hash used for all passwords: $2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm
-- Insert admin account
INSERT INTO users (id, email, password, role) VALUES
(
    'a0000000-0000-0000-0000-000000000001',
    'admin@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_ADMIN'
);

-- Insert 5 seller accounts (IDs must match User-Service)
INSERT INTO users (id, email, password, role) VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'tech@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_SELLER'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'fashion@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_SELLER'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'home@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_SELLER'
),
(
    'b0000000-0000-0000-0000-000000000004',
    'sports@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_SELLER'
),
(
    'b0000000-0000-0000-0000-000000000005',
    'books@ecommerce.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_SELLER'
);

-- Insert 5 client accounts (IDs must match User-Service)
INSERT INTO users (id, email, password, role) VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'joao.silva@email.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_CLIENT'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'maria.santos@email.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_CLIENT'
),
(
    'c0000000-0000-0000-0000-000000000003',
    'pedro.oliveira@email.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_CLIENT'
),
(
    'c0000000-0000-0000-0000-000000000004',
    'ana.costa@email.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_CLIENT'
),
(
    'c0000000-0000-0000-0000-000000000005',
    'lucas.pereira@email.com',
    '$2a$10$2oO8nx3yL2Y2WRUekf/7xuItFKmRd.w0ws6zE5RUW25KEvjMd2Olm',
    'ROLE_CLIENT'
);


-- ============================================================================
-- 2. USER-SERVICE DATABASE
-- ============================================================================
\c userservice

-- Create users table (mirrors User-Service JPA entity)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Clean existing data
DELETE FROM users;

-- Insert admin profile
INSERT INTO users (id, name, cpf, phone_number, email) VALUES
(
    'a0000000-0000-0000-0000-000000000001',
    'Administrador',
    '00000000000',
    '11999999999',
    'admin@ecommerce.com'
);

-- Insert seller profiles
INSERT INTO users (id, name, cpf, phone_number, email) VALUES
(
    'b0000000-0000-0000-0000-000000000001',
    'Loja Tech Brasil',
    '11111111111',
    '11911111111',
    'tech@ecommerce.com'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'Moda & Estilo',
    '22222222222',
    '11922222222',
    'fashion@ecommerce.com'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'Casa & Decoração',
    '33333333333',
    '11933333333',
    'home@ecommerce.com'
),
(
    'b0000000-0000-0000-0000-000000000004',
    'Esportes Brasil',
    '44444444444',
    '11944444444',
    'sports@ecommerce.com'
),
(
    'b0000000-0000-0000-0000-000000000005',
    'Livraria Cultural',
    '55555555555',
    '11955555555',
    'books@ecommerce.com'
);

-- Insert client profiles
INSERT INTO users (id, name, cpf, phone_number, email) VALUES
(
    'c0000000-0000-0000-0000-000000000001',
    'João Silva',
    '12345678901',
    '11912345678',
    'joao.silva@email.com'
),
(
    'c0000000-0000-0000-0000-000000000002',
    'Maria Santos',
    '23456789012',
    '11923456789',
    'maria.santos@email.com'
),
(
    'c0000000-0000-0000-0000-000000000003',
    'Pedro Oliveira',
    '34567890123',
    '11934567890',
    'pedro.oliveira@email.com'
),
(
    'c0000000-0000-0000-0000-000000000004',
    'Ana Costa',
    '45678901234',
    '11945678901',
    'ana.costa@email.com'
),
(
    'c0000000-0000-0000-0000-000000000005',
    'Lucas Pereira',
    '56789012345',
    '11956789012',
    'lucas.pereira@email.com'
);


-- ============================================================================
-- 3. CATALOG-SERVICE DATABASE
-- ============================================================================
\c catalogservice

-- Create products table (mirrors Catalog-Service JPA entity)
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    seller_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    attributes JSONB,
    image_urls JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Clean existing data
DELETE FROM products;

-- ============================================================================
-- PRODUCTS BY SELLER
-- ============================================================================

-- Seller 1: Loja Tech Brasil - Electronics & Tech
INSERT INTO products (id, seller_id, name, description, price, stock, active, category, sku, attributes, image_urls) VALUES
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Notebook Gamer Pro',
    'Notebook com processador Intel Core i7, 16GB RAM, SSD 512GB, placa de vídeo dedicada RTX 4060, tela 15.6" Full HD 144Hz.',
    5999.99, 15, true, 'Eletrônicos',
    'TECH-NB-001',
    '{"processador": "Intel Core i7-13700H", "ram": "16GB DDR5", "armazenamento": "512GB SSD", "placa_video": "RTX 4060", "tela": "15.6\" Full HD 144Hz", "cor": "Preto"}',
    '["https://placehold.co/600x400/1a1a2e/e94560?text=Notebook+Gamer", "https://placehold.co/600x400/1a1a2e/e94560?text=NB+Gamer+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Smartphone Ultra X',
    'Smartphone topo de linha com câmera tripla 108MP, 8GB RAM, 256GB, tela AMOLED 6.7" 120Hz, bateria 5000mAh.',
    3499.99, 30, true, 'Eletrônicos',
    'TECH-SP-001',
    '{"modelo": "Ultra X Pro Max", "ram": "8GB", "armazenamento": "256GB", "tela": "6.7\" AMOLED 120Hz", "bateria": "5000mAh", "cor": "Azul Estelar"}',
    '["https://placehold.co/600x400/16213e/0f3460?text=Smartphone", "https://placehold.co/600x400/16213e/0f3460?text=Phone+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Fone Bluetooth Premium',
    'Fone de ouvido Bluetooth 5.3 com cancelamento de ruído ativo, bateria de 40h, carregamento rápido USB-C.',
    499.99, 50, true, 'Acessórios',
    'TECH-FN-001',
    '{"conexao": "Bluetooth 5.3", "bateria": "40h", "cancelamento_ruido": true, "cor": "Branco", "tipo": "Over-ear"}',
    '["https://placehold.co/600x400/1a1a2e/0f3460?text=Fone+BT", "https://placehold.co/600x400/1a1a2e/0f3460?text=Fone+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Monitor 27" 4K',
    'Monitor IPS 27" resolução 4K, HDR10, 99% sRGB, ajuste de altura, ideal para design e produtividade.',
    2499.99, 20, true, 'Eletrônicos',
    'TECH-MN-001',
    '{"tamanho": "27\"", "resolucao": "3840x2160 (4K)", "painel": "IPS", "hdr": "HDR10", "taxa_atualizacao": "60Hz"}',
    '["https://placehold.co/600x400/16213e/e94560?text=Monitor+4K", "https://placehold.co/600x400/16213e/e94560?text=Monitor+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Teclado Mecânico RGB',
    'Teclado mecânico com switches Cherry MX, RGB por tecla, construção em alumínio, cabo USB-C destacável.',
    349.99, 40, true, 'Acessórios',
    'TECH-TC-001',
    '{"switch": "Cherry MX Red", "rgb": true, "material": "Alumínio", "conexao": "USB-C", "formato": "Full Size"}',
    '["https://placehold.co/600x400/1a1a2e/e94560?text=Teclado+RGB", "https://placehold.co/600x400/1a1a2e/e94560?text=Teclado+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Mouse Gamer sem Fio',
    'Mouse gaming ultraleve 58g, sensor óptico 26K DPI, wireless 2.4GHz/Bluetooth, bateria 70h.',
    299.99, 60, true, 'Acessórios',
    'TECH-MS-001',
    '{"peso": "58g", "dpi": "26000", "conexao": "2.4GHz + Bluetooth", "bateria": "70h", "cor": "Preto Fosco"}',
    '["https://placehold.co/600x400/16213e/0f3460?text=Mouse+Gamer", "https://placehold.co/600x400/16213e/0f3460?text=Mouse+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000001',
    'Webcam 4K Streaming',
    'Webcam 4K com autofoco, microfone embutido, ângulo de visão 90°, suporte para tripé.',
    599.99, 25, true, 'Eletrônicos',
    'TECH-WC-001',
    '{"resolucao": "4K", "autofoco": true, "microfone": true, "angulo_visao": "90°", "conexao": "USB 3.0"}',
    '["https://placehold.co/600x400/1a1a2e/e94560?text=Webcam+4K"]'
);

-- Seller 2: Moda & Estilo - Fashion
INSERT INTO products (id, seller_id, name, description, price, stock, active, category, sku, attributes, image_urls) VALUES
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Vestido Floral Verão',
    'Vestido longo em viscose, estampa floral, alças finas ajustáveis, comprimento até o tornozelo.',
    189.99, 45, true, 'Vestidos',
    'FASH-VS-001',
    '{"material": "Viscose", "comprimento": "Longo", "estampa": "Floral", "tamanhos": "P/M/G", "cor": "Azul com Flores Brancas"}',
    '["https://placehold.co/600x400/e8a87c/e94560?text=Vestido+Floral", "https://placehold.co/600x400/e8a87c/e94560?text=Vestido+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Jaqueta Jeans Vintage',
    'Jaqueta jeans estilo vintage, lavagem clara, botões de metal, gola clássica, perfeita para o dia a dia.',
    259.99, 30, true, 'Casacos',
    'FASH-JQ-001',
    '{"material": "Denim 100% algodão", "lavagem": "Clara", "estilo": "Vintage", "tamanhos": "P/M/G/GG", "cor": "Azul Claro"}',
    '["https://placehold.co/600x400/c9b99a/1a1a2e?text=Jaqueta+Jeans", "https://placehold.co/600x400/c9b99a/1a1a2e?text=Jaqueta+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Tênis Casual Premium',
    'Tênis casual em couro legítimo, sola de borracha antiderrapante, palmilha ortopédica, cadarço colorido.',
    349.99, 35, true, 'Calçados',
    'FASH-TN-001',
    '{"material": "Couro legítimo", "sola": "Borracha", "palmilha": "Ortopédica", "tamanhos": "38/39/40/41/42", "cor": "Marrom Escuro"}',
    '["https://placehold.co/600x400/8b5a2b/e8a87c?text=Tenis+Casual", "https://placehold.co/600x400/8b5a2b/e8a87c?text=Tenis+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Camisa Social Slim',
    'Camisa social slim fit em algodão egípcio, colarinho italiano, punhos duplos, 100% algodão.',
    179.99, 55, true, 'Camisas',
    'FASH-CS-001',
    '{"material": "Algodão Egípcio", "modelagem": "Slim Fit", "colarinho": "Italiano", "tamanhos": "P/M/G/GG", "cor": "Branco"}',
    '["https://placehold.co/600x400/f5f5f5/1a1a2e?text=Camisa+Social", "https://placehold.co/600x400/f5f5f5/1a1a2e?text=Camisa+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Bolsa Feminina Couro',
    'Bolsa feminina em couro legítimo, alça ajustável, compartimento interno com zíper, tamanho médio.',
    429.99, 20, true, 'Acessórios',
    'FASH-BL-001',
    '{"material": "Couro Legítimo", "tamanho": "Médio", "alca": "Ajustável", "compartimentos": "3 internos", "cor": "Preto"}',
    '["https://placehold.co/600x400/2d2d2d/e8a87c?text=Bolsa+Couro", "https://placehold.co/600x400/2d2d2d/e8a87c?text=Bolsa+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000002',
    'Relógio Elegante Prata',
    'Relógio analógico em aço inoxidável, vidro mineral, resistente à água 50m, pulseira em mesh.',
    599.99, 18, true, 'Acessórios',
    'FASH-RL-001',
    '{"material": "Aço Inoxidável", "vidro": "Mineral", "resistencia_agua": "50m", "mecanismo": "Quartzo", "cor": "Prata"}',
    '["https://placehold.co/600x400/c0c0c0/1a1a2e?text=Relogio+Prata", "https://placehold.co/600x400/c0c0c0/1a1a2e?text=Relogio+2"]'
);

-- Seller 3: Casa & Decoração - Home & Decor
INSERT INTO products (id, seller_id, name, description, price, stock, active, category, sku, attributes, image_urls) VALUES
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Sofá 3 Lugares Premium',
    'Sofá 3 lugares em couro sintético premium, estrutura em madeira de eucalipto, pés em aço escovado.',
    3899.99, 8, true, 'Móveis',
    'HOME-SF-001',
    '{"material": "Couro Sintético Premium", "estrutura": "Madeira Eucalipto", "lugares": 3, "cor": "Caramelo", "peso_suportado": "300kg"}',
    '["https://placehold.co/600x400/d4a574/1a1a2e?text=Sofa+3+Lugares", "https://placehold.co/600x400/d4a574/1a1a2e?text=Sofa+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Mesa de Jantar com 6 Cadeiras',
    'Mesa retangular em vidro temperado com base em aço escovado, acompanha 6 cadeiras estofadas.',
    2499.99, 10, true, 'Móveis',
    'HOME-MS-001',
    '{"material": "Vidro Temperado + Aço", "formato": "Retangular", "capacidade": "6 lugares", "cor": "Preto + Vidro"}',
    '["https://placehold.co/600x400/333333/d4a574?text=Mesa+Jantar", "https://placehold.co/600x400/333333/d4a574?text=Mesa+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Luminária de Chão Moderna',
    'Luminária de chão em arco, base em mármore, cúpula em tecido algodão, altura ajustável 150-190cm.',
    699.99, 15, true, 'Iluminação',
    'HOME-LM-001',
    '{"material": "Mármore + Algodão", "altura": "150-190cm", "lampada": "E27 (não inclusa)", "cor": "Branco + Dourado"}',
    '["https://placehold.co/600x400/f5e6cc/1a1a2e?text=Luminaria+Chao", "https://placehold.co/600x400/f5e6cc/1a1a2e?text=Luminaria+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Tapete Felpado 2x3m',
    'Tapete felpado macio 2x3 metros, fibra sintética antialérgica, base antiderrapante, fácil limpeza.',
    499.99, 25, true, 'Decoração',
    'HOME-TP-001',
    '{"material": "Fibra Sintética", "dimensoes": "2x3m", "antialergico": true, "antiderrapante": true, "cor": "Bege Claro"}',
    '["https://placehold.co/600x400/e8dcc8/1a1a2e?text=Tapete+Felpado", "https://placehold.co/600x400/e8dcc8/1a1a2e?text=Tapete+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Kit Panelas Antiaderentes 5 Peças',
    'Kit com 5 panelas antiaderentes em alumínio forjado, cabos baquelite, tampas de vidro temperado.',
    449.99, 28, true, 'Cozinha',
    'HOME-KT-001',
    '{"material": "Alumínio Forjado", "peças": 5, "antiaderente": true, "tampas": "Vidro Temperado", "cor": "Grafite"}',
    '["https://placehold.co/600x400/4a4a4a/d4a574?text=Kit+Panelas", "https://placehold.co/600x400/4a4a4a/d4a574?text=Panelas+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Quadro Decorativo Abstrato',
    'Quadro decorativo estilo abstrato, tela canvas 80x120cm, moldura em MDF, pintura digital de alta resolução.',
    299.99, 22, true, 'Decoração',
    'HOME-QD-001',
    '{"material": "Canvas + MDF", "dimensoes": "80x120cm", "estilo": "Abstrato", "moldura": "MDF Preto"}',
    '["https://placehold.co/600x400/e94560/1a1a2e?text=Quadro+Abstrato", "https://placehold.co/600x400/e94560/1a1a2e?text=Quadro+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000003',
    'Cafeteira Elétrica Programável',
    'Cafeteira elétrica com programação digital, jarra de vidro 1.5L, sistema antigotejamento, filtro permanente.',
    199.99, 35, true, 'Cozinha',
    'HOME-CF-001',
    '{"capacidade": "1.5L", "programavel": true, "antigotejamento": true, "filtro": "Permanente", "cor": "Inox"}',
    '["https://placehold.co/600x400/c0c0c0/1a1a2e?text=Cafeteira", "https://placehold.co/600x400/c0c0c0/1a1a2e?text=Cafeteira+2"]'
);

-- Seller 4: Esportes Brasil - Sports
INSERT INTO products (id, seller_id, name, description, price, stock, active, category, sku, attributes, image_urls) VALUES
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Bicicleta Mountain Bike Aro 29',
    'Bicicleta MTB aro 29, quadro em alumínio 6061, câmbio Shimano 21 velocidades, freio a disco hidráulico.',
    2599.99, 12, true, 'Ciclismo',
    'SPRT-BI-001',
    '{"aro": 29, "quadro": "Alumínio 6061", "marchas": "21 velocidades (Shimano)", "freio": "Disco Hidráulico", "peso": "14.5kg"}',
    '["https://placehold.co/600x400/264653/2a9d8f?text=Bicicleta+MTB", "https://placehold.co/600x400/264653/2a9d8f?text=MTB+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Halteres Ajustáveis 20kg',
    'Par de halteres ajustáveis de 2kg a 20kg cada, revestimento emborrachado, trava de segurança.',
    449.99, 20, true, 'Musculação',
    'SPRT-HL-001',
    '{"peso_maximo": "20kg cada", "ajuste": "2kg em 2kg", "material": "Ferro fundido + Emborrachado", "tipo": "Ajustável"}',
    '["https://placehold.co/600x400/2a9d8f/e9c46a?text=Halteres+20kg", "https://placehold.co/600x400/2a9d8f/e9c46a?text=Halteres+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Tênis de Corrida Pro',
    'Tênis de corrida profissional com amortecimento responsivo, cabedal em knit respirável, solado carbono.',
    899.99, 30, true, 'Corrida',
    'SPRT-TC-001',
    '{"amortecimento": "Responsivo", "cabedal": "Knit Respirável", "solado": "Carbono", "tamanhos": "38/39/40/41/42/43", "cor": "Azul Elétrico"}',
    '["https://placehold.co/600x400/e9c46a/264653?text=Tenis+Corrida", "https://placehold.co/600x400/e9c46a/264653?text=Tenis+Corrida+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Kit Yoga Completo',
    'Tapete yoga 6mm, 2 blocos EVA, alça de alongamento, cinto de yoga, bolsa para transporte.',
    199.99, 40, true, 'Yoga',
    'SPRT-YG-001',
    '{"tapete": "6mm EVA", "inclui": "2 blocos + alça + cinto + bolsa", "cor": "Lavanda"}',
    '["https://placehold.co/600x400/9b5de5/ff006e?text=Kit+Yoga", "https://placehold.co/600x400/9b5de5/ff006e?text=Yoga+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Bola de Futebol Oficial',
    'Bola de futebol campo tamanho oficial 5, costurada à mão, material PU premium, câmara butílica.',
    149.99, 50, true, 'Futebol',
    'SPRT-BF-001',
    '{"tamanho": "Oficial 5", "material": "PU Premium", "costura": "Manual", "câmara": "Butílica"}',
    '["https://placehold.co/600x400/f4a261/e76f51?text=Bola+Futebol", "https://placehold.co/600x400/f4a261/e76f51?text=Bola+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Barra de Pesos + Anilhas 50kg',
    'Barra reta cromada 1.80m + 50kg em anilhas de ferro fundido, porcas de segurança inclusas.',
    599.99, 15, true, 'Musculação',
    'SPRT-BR-001',
    '{"barra": "Cromada 1.80m", "peso_total": "50kg", "material_anilhas": "Ferro Fundido", "inclui_porcas": true}',
    '["https://placehold.co/600x400/e76f51/264653?text=Barra+Pesos"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000004',
    'Garrafa Térmica Esportiva 1L',
    'Garrafa térmica em aço inox, mantém bebida gelada por 24h ou quente por 12h, tampa com válvula.',
    89.99, 65, true, 'Acessórios',
    'SPRT-GT-001',
    '{"capacidade": "1L", "material": "Aço Inox", "temperatura_fria": "24h", "temperatura_quente": "12h", "cor": "Preto Fosco"}',
    '["https://placehold.co/600x400/264653/e9c46a?text=Garrafa+Termica"]'
);

-- Seller 5: Livraria Cultural - Books & Stationery
INSERT INTO products (id, seller_id, name, description, price, stock, active, category, sku, attributes, image_urls) VALUES
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'Box Harry Potter (7 Livros)',
    'Coleção completa com os 7 livros da série Harry Potter, capa dura, edição especial com ilustrações.',
    299.99, 40, true, 'Livros',
    'BOOK-HP-001',
    '{"coleção": "Harry Potter", "volumes": 7, "capa": "Dura", "idioma": "Português", "ilustracoes": true}',
    '["https://placehold.co/600x400/740001/ffdb00?text=Harry+Potter+Box", "https://placehold.co/600x400/740001/ffdb00?text=HP+Box+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'O Senhor dos Anéis (Edição Única)',
    'Edição especial em volume único da trilogia O Senhor dos Anéis, capa dura com mapas e apêndices.',
    149.99, 35, true, 'Livros',
    'BOOK-LOTR-001',
    '{"colecao": "O Senhor dos Anéis", "formato": "Volume Único", "capa": "Dura", "idioma": "Português", "mapas": true}',
    '["https://placehold.co/600x400/2d572c/c8a951?text=Senhor+Aneis", "https://placehold.co/600x400/2d572c/c8a951?text=LOTR+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'Kit Cadernos Inteligentes 3x',
    'Kit com 3 cadernos inteligentes A5, 200 páginas cada, papel 90g, capa dura, marcadores inclusos.',
    89.99, 50, true, 'Papelaria',
    'BOOK-CD-001',
    '{"quantidade": 3, "tamanho": "A5", "paginas": 200, "papel": "90g", "capa": "Dura", "cores": "Sortidas"}',
    '["https://placehold.co/600x400/264653/2a9d8f?text=Cadernos+Kit", "https://placehold.co/600x400/264653/2a9d8f?text=Cadernos+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'Caneta Tinteiro Premium',
    'Caneta tinteiro em resina acrílica, bico de ouro 14k, sistema de carga por cartucho ou conversor.',
    449.99, 15, true, 'Papelaria',
    'BOOK-CT-001',
    '{"material": "Resina Acrílica", "bico": "Ouro 14k", "carga": "Cartucho/Conversor", "cor": "Azul Marinho"}',
    '["https://placehold.co/600x400/1a1a2e/e94560?text=Caneta+Tinteiro", "https://placehold.co/600x400/1a1a2e/e94560?text=Caneta+2"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    '1984 - George Orwell (Capa Dura)',
    'Edição especial em capa dura do clássico distópico 1984, com prefácio e notas explicativas.',
    49.99, 60, true, 'Livros',
    'BOOK-1984-001',
    '{"autor": "George Orwell", "capa": "Dura", "idioma": "Português", "genero": "Distopia", "paginas": 416}',
    '["https://placehold.co/600x400/2c3e50/e74c3c?text=1984+Orwell"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'Marca Textos Pastel (6 Cores)',
    'Kit 6 marca-textos neon pastel, ponta chanfrada, tinta a base d''água, secagem rápida.',
    29.99, 80, true, 'Papelaria',
    'BOOK-MT-001',
    '{"quantidade": 6, "cores": "Pastel", "ponta": "Chanfrada", "tipo": "Neon Pastel"}',
    '["https://placehold.co/600x400/ffd6e0/e94560?text=Marca+Textos"]'
),
(
    gen_random_uuid(),
    'b0000000-0000-0000-0000-000000000005',
    'Agenda 2026 Executiva',
    'Agenda 2026 em capa de couro sintético, argola metálica, bolsa para caneta, elástico de fechamento.',
    79.99, 45, true, 'Papelaria',
    'BOOK-AG-001',
    '{"ano": 2026, "capa": "Couro Sintético", "formato": "Médio", "fechamento": "Elástico", "cor": "Marrom"}',
    '["https://placehold.co/600x400/8b4513/f5deb3?text=Agenda+2026"]'
);

-- ============================================================================
-- Summary of inserted data
-- ============================================================================
-- Total Users (Auth): 11 (1 admin, 5 sellers, 5 clients)
-- Total Users (User): 11 (1 admin, 5 sellers, 5 clients)
-- Total Products: 34 (distributed across 5 sellers)
--
-- Credentials for testing:
--   Admin:  admin@ecommerce.com / admin123
--   Seller: tech@ecommerce.com / seller123
--           fashion@ecommerce.com / seller123
--           home@ecommerce.com / seller123
--           sports@ecommerce.com / seller123
--           books@ecommerce.com / seller123
--   Client: joao.silva@email.com / client123
--           maria.santos@email.com / client123
--           pedro.oliveira@email.com / client123
--           ana.costa@email.com / client123
--           lucas.pereira@email.com / client123
-- ============================================================================