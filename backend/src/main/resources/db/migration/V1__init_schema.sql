-- V1__init_schema.sql: Initial Relational Schema for KiranaLink / Smart Dukaan

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    phone_number VARCHAR(50),
    google_id VARCHAR(255) UNIQUE,
    shop_name VARCHAR(255),
    avatar TEXT,
    address TEXT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    default_voice_language VARCHAR(20) DEFAULT 'en',
    fallback_voice_language VARCHAR(20) DEFAULT 'en',
    voice_language_policy VARCHAR(20) DEFAULT 'hybrid',
    enable_voice_language_menu BOOLEAN DEFAULT TRUE,
    supported_voice_languages VARCHAR(255) DEFAULT 'en,hi,te',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255),
    khata_balance DECIMAL(12, 2) DEFAULT 0.00,
    trust_score INT DEFAULT 600,
    khata_score INT DEFAULT 600,
    khata_limit DECIMAL(12, 2) DEFAULT 3000.00,
    last_score_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    next_call_date BIGINT,
    recovery_status VARCHAR(50),
    recovery_notes TEXT,
    visit_validation INT DEFAULT 0,
    loyalty_points INT DEFAULT 0,
    total_purchases DECIMAL(12, 2) DEFAULT 0.00,
    preferred_language VARCHAR(20) DEFAULT 'en',
    preferred_voice_language VARCHAR(20) DEFAULT 'en',
    lock_voice_language BOOLEAN DEFAULT FALSE,
    last_detected_voice_language VARCHAR(20),
    last_voice_language_confidence DECIMAL(5, 2) DEFAULT 0.00,
    voice_language_source VARCHAR(30) DEFAULT 'shop_default',
    voice_language_updated_at TIMESTAMP NULL,
    whatsapp_last_inbound_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cust_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS customer_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    shopkeeper_id BIGINT NOT NULL,
    balance DECIMAL(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cust_shop (customer_id, shopkeeper_id),
    CONSTRAINT fk_ca_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    cost_price DECIMAL(12, 2),
    stock DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    category VARCHAR(100),
    unit VARCHAR(50),
    icon VARCHAR(50),
    normalized_name VARCHAR(255),
    hsn_code VARCHAR(50),
    gst_rate DECIMAL(5, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_prod_shop (shopkeeper_id),
    CONSTRAINT fk_prod_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS global_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    stock DECIMAL(12, 2) DEFAULT 0.00,
    category VARCHAR(100),
    unit VARCHAR(50),
    icon VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bill_shop (shopkeeper_id),
    CONSTRAINT fk_bill_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bill_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_bi_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_bi_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    bill_id BIGINT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    payment_mode VARCHAR(50) DEFAULT 'ledger',
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_le_cust_shop (customer_id, shopkeeper_id),
    CONSTRAINT fk_le_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_le_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_le_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(50) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    attempts INT DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gst_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    normalized_name VARCHAR(255) NOT NULL UNIQUE,
    hsn_code VARCHAR(50) NOT NULL,
    gst_rate DECIMAL(5, 2) NOT NULL DEFAULT 5.00,
    category VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gst_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    bill_id BIGINT NULL,
    total_base_amount DECIMAL(12, 2) NOT NULL,
    total_cgst DECIMAL(12, 2) NOT NULL,
    total_sgst DECIMAL(12, 2) NOT NULL,
    total_gst DECIMAL(12, 2) NOT NULL,
    grand_total DECIMAL(12, 2) NOT NULL,
    invoice_type VARCHAR(20) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_gi_shop_period (shopkeeper_id, year, month),
    CONSTRAINT fk_gi_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_gi_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_gi_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gst_invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gst_invoice_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    hsn_code VARCHAR(50),
    gst_rate DECIMAL(5, 2) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    price_includes_gst BOOLEAN DEFAULT TRUE,
    base_amount DECIMAL(12, 2) NOT NULL,
    cgst_amount DECIMAL(12, 2) NOT NULL,
    sgst_amount DECIMAL(12, 2) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_gii_invoice FOREIGN KEY (gst_invoice_id) REFERENCES gst_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gst_ledger (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    gst_invoice_id BIGINT NOT NULL,
    reference_id BIGINT NULL,
    reference_type VARCHAR(50) NULL,
    total_base_amount DECIMAL(12, 2) NOT NULL,
    total_cgst DECIMAL(12, 2) NOT NULL,
    total_sgst DECIMAL(12, 2) NOT NULL,
    total_gst DECIMAL(12, 2) NOT NULL,
    grand_total DECIMAL(12, 2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_gl_shop_period (shopkeeper_id, year, month, type),
    CONSTRAINT fk_gl_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_gl_invoice FOREIGN KEY (gst_invoice_id) REFERENCES gst_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_code VARCHAR(100),
    mfg_date DATE NULL,
    expiry_date DATE NULL,
    quantity_received DECIMAL(12, 2) NOT NULL,
    quantity_available DECIMAL(12, 2) NOT NULL,
    cost_price_per_unit DECIMAL(12, 2),
    selling_price_snapshot DECIMAL(12, 2),
    supplier_bill_id BIGINT NULL,
    source VARCHAR(50) DEFAULT 'manual',
    status VARCHAR(30) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ib_shop_expiry (shopkeeper_id, expiry_date, status),
    CONSTRAINT fk_ib_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ib_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS expiry_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    days_to_expiry INT NOT NULL,
    risk_bucket VARCHAR(50) NOT NULL,
    suggested_action VARCHAR(50) NOT NULL,
    action_status VARCHAR(50) DEFAULT 'open',
    action_meta TEXT,
    assigned_to BIGINT NULL,
    acted_at TIMESTAMP NULL,
    last_evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_sent_at TIMESTAMP NULL,
    last_notified_bucket VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ea_shop_bucket (shopkeeper_id, risk_bucket, action_status),
    CONSTRAINT fk_ea_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ea_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_ea_batch FOREIGN KEY (batch_id) REFERENCES inventory_batches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS waste_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    unit_cost DECIMAL(12, 2) NOT NULL,
    estimated_loss DECIMAL(12, 2) NOT NULL,
    disposal_mode VARCHAR(50) DEFAULT 'discarded',
    notes TEXT,
    logged_by BIGINT NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_wl_shop_time (shopkeeper_id, logged_at),
    CONSTRAINT fk_wl_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_wl_batch FOREIGN KEY (batch_id) REFERENCES inventory_batches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS discount_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    shopkeeper_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    description TEXT,
    discount_type VARCHAR(30) NOT NULL,
    discount_value DECIMAL(12, 2) NOT NULL,
    min_purchase DECIMAL(12, 2) DEFAULT 0.00,
    max_uses INT DEFAULT 100,
    used_count INT DEFAULT 0,
    valid_from TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_for VARCHAR(50) DEFAULT 'manual',
    linked_batch_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dc_code_shop (code, shopkeeper_id),
    CONSTRAINT fk_dc_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_dc_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS discount_usage_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    discount_id BIGINT NOT NULL,
    bill_id BIGINT NULL,
    customer_id BIGINT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_duh_discount FOREIGN KEY (discount_id) REFERENCES discount_codes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS supplier_bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) DEFAULT 0.00,
    item_count INT DEFAULT 0,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'processed',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sb_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS supplier_bill_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_bill_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    unit VARCHAR(50),
    total_amount DECIMAL(12, 2) NOT NULL,
    cost_price DECIMAL(12, 2),
    selling_price DECIMAL(12, 2),
    CONSTRAINT fk_sbi_bill FOREIGN KEY (supplier_bill_id) REFERENCES supplier_bills(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS group_buys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    market_price DECIMAL(12, 2) NOT NULL,
    deal_price DECIMAL(12, 2),
    target_units INT DEFAULT 10,
    current_units INT DEFAULT 0,
    image_url VARCHAR(500),
    category VARCHAR(100) DEFAULT 'General',
    anchor_shop VARCHAR(255) DEFAULT 'Local Hub',
    expires_at TIMESTAMP NULL,
    ai_insight TEXT,
    status VARCHAR(30) DEFAULT 'active',
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gb_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS group_buy_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_gbm_group FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS group_buy_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_buy_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_gbp_group FOREIGN KEY (group_buy_id) REFERENCES group_buys(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id VARCHAR(100) NOT NULL UNIQUE,
    client_name VARCHAR(255) NOT NULL,
    client_email VARCHAR(255),
    client_phone VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    due_date TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'unpaid',
    reminder_level INT DEFAULT 0,
    last_contacted_at TIMESTAMP NULL,
    payment_link VARCHAR(500) DEFAULT '',
    promised_date TIMESTAMP NULL,
    next_retry_at TIMESTAMP NULL,
    last_intent VARCHAR(100) DEFAULT 'UNKNOWN',
    ai_confidence DECIMAL(5, 2) DEFAULT 0.00,
    no_speech_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_inv_phone (client_phone),
    INDEX idx_inv_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS invoice_reminder_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    message_content TEXT NOT NULL,
    delivery_status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_irh_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS voice_call_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_sid VARCHAR(100) NOT NULL UNIQUE,
    invoice_id VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    stage VARCHAR(50) DEFAULT 'OPENING',
    status VARCHAR(50) DEFAULT 'active',
    turn_count INT DEFAULT 0,
    partial_amount_now DECIMAL(12, 2) DEFAULT 0.00,
    remaining_amount DECIMAL(12, 2) DEFAULT 0.00,
    promised_date TIMESTAMP NULL,
    outcome_intent VARCHAR(100) DEFAULT 'UNKNOWN',
    confidence DECIMAL(5, 2) DEFAULT 0.00,
    final_summary TEXT,
    detected_language VARCHAR(20) DEFAULT 'en',
    language_confidence DECIMAL(5, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS whatsapp_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopkeeper_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_message TEXT NOT NULL,
    parsed_text TEXT,
    media_url VARCHAR(500),
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'confirmed',
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_mode VARCHAR(50) DEFAULT 'cash_pickup',
    source_window_24h BOOLEAN DEFAULT TRUE,
    reference_code VARCHAR(100),
    review_state VARCHAR(50) DEFAULT 'none',
    review_reason TEXT,
    converted_bill_id BIGINT NULL,
    converted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_wo_shop (shopkeeper_id),
    CONSTRAINT fk_wo_shopkeeper FOREIGN KEY (shopkeeper_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_wo_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS whatsapp_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    whatsapp_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity DECIMAL(12, 2) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_woi_order FOREIGN KEY (whatsapp_order_id) REFERENCES whatsapp_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_woi_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
