-- Create schema
CREATE SCHEMA IF NOT EXISTS predictflow;

-- Create users table
CREATE TABLE IF NOT EXISTS predictflow.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create auth_blacklist table
CREATE TABLE IF NOT EXISTS predictflow.auth_blacklist (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Create event_audit table
CREATE TABLE IF NOT EXISTS predictflow.event_audit (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB,
    source VARCHAR(255),
    trace_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS predictflow.transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default user
INSERT INTO predictflow.users (email, name, password, role) 
VALUES ('admin@example.com', 'Admin User', '$2a$12$abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQR', 'ADMIN')
ON CONFLICT DO NOTHING;
