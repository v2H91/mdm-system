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

    -- Các trường trạng thái và phê duyệt
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approved_by VARCHAR(100),
    approved_at TIMESTAMP NULL,

    -- Các trường cho Reject
    rejected_by VARCHAR(100),
    rejected_at TIMESTAMP NULL,
    rejected_reason TEXT,

    -- Các trường Audit hệ thống
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index cho status để Filter nhanh
CREATE INDEX idx_org_status ON organizations(status);

-- 3. Table Addresses
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_number VARCHAR(255),
    street VARCHAR(255),

    -- Chỉ giữ lại 1 cột tham chiếu đến cấp hành chính thấp nhất (Xã/Phường)
    -- Đảm bảo cùng kiểu dữ liệu VARCHAR(10) và collation với bảng locations
    location_code VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,

    -- Cột lưu địa chỉ đầy đủ được sinh tự động từ Java
    full_address TEXT,

    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Ràng buộc khóa ngoại duy nhất tới bảng locations
    CONSTRAINT fk_address_location FOREIGN KEY (location_code) REFERENCES locations(code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Thêm index để tối ưu các câu truy vấn tìm kiếm địa chỉ theo vùng
CREATE INDEX idx_address_location_code ON addresses(location_code);

-- 4. Table Org_Addresses
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
                                                entity_name VARCHAR(50) NOT NULL,    -- Ví dụ: 'organization', 'address'
    field_name VARCHAR(50) NOT NULL,     -- Ví dụ: 'tax_code', 'email'
    regex_pattern VARCHAR(255),          -- Biểu thức chính quy để validate
    error_message VARCHAR(255),          -- Thông báo lỗi tùy chỉnh
    is_read_only BOOLEAN DEFAULT FALSE,  -- Nếu TRUE, không cho phép cập nhật trường này
    is_required BOOLEAN DEFAULT FALSE,   -- Bổ sung check bắt buộc nhập
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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