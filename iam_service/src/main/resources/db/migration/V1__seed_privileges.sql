START TRANSACTION;

-- Chèn tối thiểu các cột có thật trong bảng privileges của bạn
-- (theo ảnh: có is_deleted, không có is_system)
-- Nếu privilege_name NOT NULL: giữ như dưới; nếu cho phép NULL có thể bỏ cột privilege_name.

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'READ_ONLY', 'READ_ONLY', 'Default read-only privilege', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'READ_ONLY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'TEST_ORDER_CREATE', 'TEST_ORDER_CREATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'TEST_ORDER_CREATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'TEST_ORDER_MODIFY', 'TEST_ORDER_MODIFY', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'TEST_ORDER_MODIFY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'TEST_ORDER_DELETE', 'TEST_ORDER_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'TEST_ORDER_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'TEST_ORDER_REVIEW', 'TEST_ORDER_REVIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'TEST_ORDER_REVIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'COMMENT_ADD', 'COMMENT_ADD', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'COMMENT_ADD');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'COMMENT_MODIFY', 'COMMENT_MODIFY', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'COMMENT_MODIFY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'COMMENT_DELETE', 'COMMENT_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'COMMENT_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'CONFIG_VIEW', 'CONFIG_VIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'CONFIG_VIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'CONFIG_CREATE', 'CONFIG_CREATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'CONFIG_CREATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'CONFIG_MODIFY', 'CONFIG_MODIFY', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'CONFIG_MODIFY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'CONFIG_DELETE', 'CONFIG_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'CONFIG_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'USER_VIEW', 'USER_VIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'USER_VIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'USER_CREATE', 'USER_CREATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'USER_CREATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'USER_MODIFY', 'USER_MODIFY', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'USER_MODIFY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'USER_DELETE', 'USER_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'USER_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'USER_LOCK_UNLOCK', 'USER_LOCK_UNLOCK', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'USER_LOCK_UNLOCK');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'ROLE_VIEW', 'ROLE_VIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'ROLE_VIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'ROLE_CREATE', 'ROLE_CREATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'ROLE_CREATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'ROLE_UPDATE', 'ROLE_UPDATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'ROLE_UPDATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'ROLE_DELETE', 'ROLE_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'ROLE_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'EVENT_LOG_VIEW', 'EVENT_LOG_VIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'EVENT_LOG_VIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'REAGENT_ADD', 'REAGENT_ADD', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'REAGENT_ADD');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'REAGENT_MODIFY', 'REAGENT_MODIFY', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'REAGENT_MODIFY');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'REAGENT_DELETE', 'REAGENT_DELETE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'REAGENT_DELETE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'INSTRUMENT_ADD', 'INSTRUMENT_ADD', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'INSTRUMENT_ADD');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'INSTRUMENT_VIEW', 'INSTRUMENT_VIEW', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'INSTRUMENT_VIEW');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'INSTRUMENT_ACTIVATE_DEACTIVATE', 'INSTRUMENT_ACTIVATE_DEACTIVATE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'INSTRUMENT_ACTIVATE_DEACTIVATE');

INSERT INTO privileges (privilege_id, privilege_code, privilege_name, privilege_description, is_deleted, created_at)
SELECT UUID(), 'BLOOD_TEST_EXECUTE', 'BLOOD_TEST_EXECUTE', 'Seeded by Flyway', 0, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM privileges WHERE privilege_code = 'BLOOD_TEST_EXECUTE');

COMMIT;