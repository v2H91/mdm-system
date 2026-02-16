🏢 Hệ thống Quản lý Master Data (MDM) cho Tổ chức & Địa chỉ
Hệ thống cung cấp giải pháp quản lý dữ liệu dùng chung (Master Data) tập trung cho thông tin Tổ chức (Organization) và Địa chỉ (Address) dựa trên kiến trúc RESTful API. Đây là "Nguồn tin cậy duy nhất" (Single Source of Truth) giúp đồng bộ dữ liệu giữa các hệ thống vệ tinh trong doanh nghiệp.

🚀 Tính năng nổi bật
Quản lý Địa chỉ phân cấp (Hierarchy): Hỗ trợ cấu hình đa cấp (Tỉnh/Thành -> Quận/Huyện -> Phường/Xã) với cấu hình cây dữ liệu tối ưu.

Validation & Normalization động: Các quy tắc kiểm tra (Regex) được cấu hình linh hoạt trong cơ sở dữ liệu, cho phép thay đổi logic kiểm tra mà không cần thay đổi mã nguồn.

Global/Local Data Mapping: Cơ chế ánh xạ trường dữ liệu giúp các hệ thống con (ERP, CRM, POS) dễ dàng tích hợp và hiểu dữ liệu theo ngôn ngữ riêng của chúng.

Duplicate Detection: Kiểm tra trùng lặp mã số thuế và sử dụng thuật toán so sánh chuỗi để cảnh báo trùng lặp thông tin tổ chức.

Audit Trail: Ghi lại toàn bộ lịch sử thay đổi (ai sửa, sửa lúc nào, giá trị cũ/mới) dưới dạng JSON để phục vụ đối soát.

Soft Delete: Cơ chế xóa mềm đảm bảo toàn vẹn tham chiếu cho các hệ thống đang sử dụng dữ liệu master.

Hiệu năng cao: Tích hợp Redis Cache và Java 21 Virtual Threads để tối ưu hóa tốc độ truy xuất và xử lý đồng thời.

🛠 Tech Stack
Backend: Java 21 (LTS), Spring Boot 3.x

Data Access: Spring Data JPA (Hibernate 6)

Database: MySQL 8.0

Caching: Redis

Migration: Flyway

Documentation: Swagger / OpenAPI 3.0

DevOps: Docker, Docker Compose

🏗 Kiến trúc Hệ thống
Hệ thống được thiết kế theo mô hình Service-Oriented Architecture (SOA) với các lớp tách biệt:

API Layer: Cung cấp chuẩn RESTful với các Versioning (v1, v2).

Service Layer: Chứa logic nghiệp vụ phức tạp về Validation, Normalization và xử lý Event.

Data Layer: Sử dụng JPA EntityGraph để giải quyết bài toán N+1 query khi fetch dữ liệu địa chỉ lồng nhau.

Integration Layer: Sử dụng cơ chế Event-driven để thông báo thay đổi dữ liệu sang các hệ thống khác.

📋 Cấu trúc Database quan trọng
locations: Lưu trữ phân cấp địa chỉ sử dụng kỹ thuật path (ví dụ: /1/3/20) để truy vấn subtree nhanh chóng.

validation_rules: Lưu trữ các biểu thức chính quy (Regex) và thông điệp lỗi cho từng trường thông tin.

data_mappings: Quản lý cấu hình ánh xạ trường (local_field vs global_field) theo từng hệ thống nguồn.

audit_logs: Lưu trữ vết thay đổi dữ liệu dưới dạng JSON.

🚦 Hướng dẫn cài đặt
Yêu cầu hệ thống
JDK 21

Docker & Docker Compose

Maven 3.9+

Khởi chạy với Docker
Bash

# Clone dự án
git clone https://github.com/your-repo/master-data-management.git

# Khởi chạy Database & Redis
docker-compose up -d

# Build và chạy ứng dụng
mvn spring-boot:run
Truy cập tài liệu API
Sau khi ứng dụng khởi chạy, bạn có thể truy cập Swagger UI tại: http://localhost:8080/swagger-ui.html

📝 TODO (Kế hoạch phát triển)
[ ] Triển khai cơ chế Fuzzy Search cho tên tổ chức bằng Elasticsearch.

[ ] Phát triển Dashboard cho Admin quản lý Rule và Mapping.

[ ] Tích hợp Kafka để thay thế cho Local Event nếu hệ thống mở rộng quy mô.