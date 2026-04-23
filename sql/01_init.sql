-- =========================================================
-- 01_init.sql
-- Базовая схема БД для OTP Service
-- =========================================================

-- ---------------------------------------------------------
-- Таблица пользователей
-- ---------------------------------------------------------
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       login VARCHAR(50) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                       CONSTRAINT chk_users_role
                           CHECK (role IN ('ADMIN', 'USER'))
);

-- ---------------------------------------------------------
-- Таблица конфигурации OTP
-- По ТЗ в таблице должна быть только одна запись
-- ---------------------------------------------------------
CREATE TABLE otp_config (
                            id SMALLINT PRIMARY KEY,
                            code_length INTEGER NOT NULL,
                            ttl_seconds INTEGER NOT NULL,
                            updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

                            CONSTRAINT chk_otp_config_single_row
                                CHECK (id = 1),

                            CONSTRAINT chk_otp_config_code_length
                                CHECK (code_length BETWEEN 4 AND 12),

                            CONSTRAINT chk_otp_config_ttl_seconds
                                CHECK (ttl_seconds BETWEEN 30 AND 3600)
);

-- ---------------------------------------------------------
-- Таблица OTP-кодов
-- ---------------------------------------------------------
CREATE TABLE otp_codes (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           operation_id VARCHAR(100) NOT NULL,
                           code VARCHAR(20) NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           channel VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                           expires_at TIMESTAMP NOT NULL,
                           used_at TIMESTAMP NULL,

                           CONSTRAINT fk_otp_codes_user
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                           CONSTRAINT chk_otp_codes_status
                               CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),

                           CONSTRAINT chk_otp_codes_channel
                               CHECK (channel IN ('EMAIL', 'SMS', 'TELEGRAM', 'FILE'))
);

-- ---------------------------------------------------------
-- Индексы
-- ---------------------------------------------------------
CREATE INDEX idx_otp_codes_user_id ON otp_codes(user_id);
CREATE INDEX idx_otp_codes_operation_id ON otp_codes(operation_id);
CREATE INDEX idx_otp_codes_status ON otp_codes(status);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes(expires_at);

-- ---------------------------------------------------------
-- Стартовая конфигурация OTP
-- ---------------------------------------------------------
INSERT INTO otp_config (id, code_length, ttl_seconds)
VALUES (1, 6, 300);