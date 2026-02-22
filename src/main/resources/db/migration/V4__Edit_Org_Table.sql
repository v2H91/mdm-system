CREATE TABLE IF NOT EXISTS organization_histories (
                                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                      org_id BIGINT NOT NULL,
                                                      action_type ENUM('CREATE', 'UPDATE', 'APPROVE', 'REJECT') NOT NULL,

    -- Lưu thông tin thay đổi dưới dạng JSON để linh hoạt
    -- Ví dụ: {"legal_name": {"old": "Công ty A", "new": "Công ty A+"}}
    changes JSON,

    reason TEXT, -- Lý do nếu là REJECT hoặc ghi chú sửa đổi
    performed_by VARCHAR(100), -- Email hoặc Username người thực hiện
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_org FOREIGN KEY (org_id) REFERENCES organizations(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_history_org_id ON organization_histories(org_id);

ALTER TABLE organizations
    ADD COLUMN pending_data JSON NULL AFTER status,
    ADD COLUMN is_editing BOOLEAN DEFAULT FALSE AFTER pending_data;