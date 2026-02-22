-- 1. Table Roles
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE -- Ví dụ: ROLE_ADMIN, ROLE_APPROVER, ROLE_EDITOR
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Table Users
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Table User_Roles (N-N)
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id),
                            FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1. Insert các Role cơ bản cho hệ thống MDM
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_APPROVER'); -- Người duyệt dữ liệu
INSERT INTO roles (name) VALUES ('ROLE_EDITOR');   -- Người nhập liệu/sửa đổi
INSERT INTO roles (name) VALUES ('ROLE_VIEWER');   -- Người chỉ được xem
INSERT INTO roles (name) VALUES ('EXTERNAL_SERVICE');

-- 2. Insert User Admin (Username: admin / Password: password123)
-- Lưu ý: Mật khẩu dưới đây là chuỗi đã mã hóa bằng BCrypt ($2a$10$...)
INSERT INTO users (username, password, email, is_active)
VALUES ('admin', '$2a$10$6GJzPW.mhA6s8fNlCzXU3.QPhELi1JV5W7GsmE1sq3H6h8uN8bOtK', 'admin@mdm.com', TRUE);

-- 3. Gán quyền ADMIN cho user vừa tạo (Giả định id user=1, id role=1)
INSERT INTO user_roles (user_id, role_id)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
       );

CREATE TABLE external_clients (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  client_name VARCHAR(100) NOT NULL UNIQUE, -- Tên đối tác (ví dụ: ERP_SYSTEM)
                                  client_id VARCHAR(50) NOT NULL UNIQUE,   -- Dùng làm API Key
                                  public_key TEXT NOT NULL,                -- Lưu Public Key của họ dưới dạng Base64
                                  is_active BOOLEAN DEFAULT TRUE
);