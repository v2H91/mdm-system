-- Dữ liệu địa chỉ mẫu
-- Dữ liệu Tỉnh (PROVINCE)
INSERT INTO locations (code, name, name_with_type, type, parent_code, path_with_type)
VALUES ('01', 'Hà Nội', 'Thành phố Hà Nội', 'PROVINCE', NULL, 'Thành phố Hà Nội'),
       ('79', 'Hồ Chí Minh', 'Thành phố Hồ Chí Minh', 'PROVINCE', NULL, 'Thành phố Hồ Chí Minh');

-- Dữ liệu Xã (WARD) trỏ thẳng về Tỉnh
INSERT INTO locations (code, name, name_with_type, type, parent_code, path_with_type)
VALUES ('00001', 'Dịch Vọng Hậu', 'Phường Dịch Vọng Hậu', 'WARD', '01', 'Phường Dịch Vọng Hậu, Thành phố Hà Nội'),
       ('00002', 'Hàng Bạc', 'Phường Hàng Bạc', 'WARD', '01', 'Phường Hàng Bạc, Thành phố Hà Nội'),
       ('40001', 'Bến Nghé', 'Phường Bến Nghé', 'WARD', '79', 'Phường Bến Nghé, Thành phố Hồ Chí Minh');

-- Dữ liệu Validation Rules
INSERT INTO validation_rules (entity_name, field_name, regex_pattern, error_message)
VALUES ('ORGANIZATION', 'email', '^[A-Za-z0-9+_.-]+@(.+)$', 'Định dạng Email không hợp lệ'),
       ('ORGANIZATION', 'phone', '^0[0-9]{9}$', 'Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số'),
       ('ADDRESS', 'zip_code', '^[0-9]{5,6}$', 'Mã bưu điện phải có 5 hoặc 6 chữ số'),
       ('ORGANIZATION', 'website', '^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$',
        'Website không đúng định dạng'),
       ('ORGANIZATION', 'short_name', '^[A-Z0-9 ]{2,20}$', 'Tên viết tắt phải là chữ in hoa, từ 2-20 ký tự'),
       ('ADDRESS', 'house_number', '^[a-zA-Z0-9/\\-\\ ]+$', 'Số nhà chứa ký tự không hợp lệ'),
       ('ORGANIZATION', 'representative', '^.{2,100}$', 'Tên người đại diện phải từ 2-100 ký tự'),
       ('ORGANIZATION', 'founded_date', '^\\d{4}-\\d{2}-\\d{2}$', 'Ngày thành lập phải theo định dạng YYYY-MM-DD');

-- Dữ liệu Mapping mẫu
INSERT INTO data_mappings (source_system, entity_type, local_field, global_field)
VALUES ('CRM_SYSTEM', 'ORGANIZATION', 'Company_Name', 'legal_name'),
       ('CRM_SYSTEM', 'ORGANIZATION', 'Tax_ID', 'tax_code');

-- Dữ liệu Tổ chức mẫu
INSERT INTO organizations (tax_code, legal_name, short_name, status)
VALUES ('0100100123', 'Ngân hàng Thương mại Cổ phần Ngoại thương Việt Nam', 'Vietcombank', 'ACTIVE'),
       ('0101234568', 'Tập đoàn Công nghiệp - Viễn thông Quân đội', 'Viettel', 'ACTIVE'),
       ('0100109106', 'Tập đoàn Bưu chính Viễn thông Việt Nam', 'VNPT', 'ACTIVE'),
       ('0301453456', 'Công ty Cổ phần Sữa Việt Nam', 'Vinamilk', 'ACTIVE'),
       ('0102030405', 'Tập đoàn FPT', 'FPT Group', 'ACTIVE'),
       ('0312345678', 'Công ty TNHH Nước giải khát Coca-Cola Việt Nam', 'Coca-Cola', 'ACTIVE'),
       ('0400123123', 'Công ty Cổ phần Hàng không Vietjet', 'Vietjet Air', 'INACTIVE'),
       ('0105678901', 'Công ty Cổ phần Tập đoàn Masan', 'Masan Group', 'PENDING'),
       ('0309876543', 'Ngân hàng TMCP Kỹ thương Việt Nam', 'Techcombank', 'ACTIVE'),
       ('0101112223', 'Tổng Công ty Hàng không Việt Nam', 'Vietnam Airlines', 'ACTIVE');


-- Sửa lại dữ liệu Address để khớp với ward_code và province_code dạng String
INSERT INTO addresses (house_number, street, location_code, full_address)
VALUES ('198', 'Trần Quang Khải', '40001', '198 Trần Quang Khải, Phường Bến Nghé, Thành phố Hồ Chí Minh'),
       ('01', 'Giang Văn Minh', '00001', '01 Giang Văn Minh, Phường Dịch Vọng Hậu, Thành phố Hà Nội'),
       ('Số 1', 'Duy Tân', '00001', 'Số 1 Duy Tân, Phường Dịch Vọng Hậu, Thành phố Hà Nội'),
       ('72', 'Lê Thánh Tôn', '40001', '72 Lê Thánh Tôn, Phường Bến Nghé, Thành phố Hồ Chí Minh'),
       ('54A', 'Nguyễn Chí Thanh', '00001', '54A Nguyễn Chí Thanh, Thành phố Hà Nội');