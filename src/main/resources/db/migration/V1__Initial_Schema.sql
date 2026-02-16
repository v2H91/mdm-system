CREATE TABLE IF NOT EXISTS locations (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           code VARCHAR(10) NOT NULL UNIQUE, -- Lưu "01", "002"
                           name VARCHAR(255) NOT NULL,       -- "Hoàn Kiếm"
                           name_with_type VARCHAR(255),      -- "Quận Hoàn Kiếm"
                           slug VARCHAR(255),                -- "hoan-kiem"
                           type VARCHAR(50),                -- "quan", "tinh"
                           parent_code VARCHAR(10),          -- "01" (Thay vì parent_id để dễ map data)
                           path_with_type TEXT,              -- "Quận Hoàn Kiếm, Thành phố Hà Nội"
                           is_deleted BOOLEAN DEFAULT FALSE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table Organizations
CREATE TABLE IF NOT EXISTS organizations (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             tax_code VARCHAR(50) NOT NULL UNIQUE,
    legal_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING') DEFAULT 'ACTIVE',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table Addresses
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_number VARCHAR(255),
    street VARCHAR(255),
    -- Đảm bảo cùng kiểu dữ liệu và collation với bảng locations
    ward_code VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    province_code VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    full_address TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addr_ward FOREIGN KEY (ward_code) REFERENCES locations(code),
    CONSTRAINT fk_addr_province FOREIGN KEY (province_code) REFERENCES locations(code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table Org_Addresses (SỬA LỖI TẠI ĐÂY)
CREATE TABLE IF NOT EXISTS org_addresses (
    org_id BIGINT NOT NULL,
    address_id BIGINT NOT NULL,
    address_type ENUM('HEADQUARTER', 'BRANCH', 'WAREHOUSE') DEFAULT 'BRANCH',
    PRIMARY KEY (org_id, address_id),
    FOREIGN KEY (org_id) REFERENCES organizations(id),
    FOREIGN KEY (address_id) REFERENCES addresses(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Table Validation Rules
CREATE TABLE IF NOT EXISTS validation_rules (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                entity_name VARCHAR(50) NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    regex_pattern VARCHAR(255) NOT NULL,
    error_message VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Table Data Mappings
CREATE TABLE IF NOT EXISTS data_mappings (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             source_system VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    local_field VARCHAR(100) NOT NULL,
    global_field VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Table Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          entity_name VARCHAR(50),
    entity_id BIGINT,
    action VARCHAR(10),
    old_value JSON,
    new_value JSON,
    modified_by VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;