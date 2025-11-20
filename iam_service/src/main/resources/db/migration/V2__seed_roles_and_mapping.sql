START TRANSACTION;

-- roles(role_id, role_code, role_name, role_description, is_system, is_deleted, created_at)

INSERT INTO roles (role_id, role_code, role_name, role_description, is_system, is_deleted, created_at)
SELECT UUID(), 'ROLE_USER', 'Standard User', 'Default role for normal users', TRUE, FALSE, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'ROLE_USER');

INSERT INTO roles (role_id, role_code, role_name, role_description, is_system, is_deleted, created_at)
SELECT UUID(), 'ROLE_ADMIN', 'Administrator', 'System administrator with full privileges', TRUE, FALSE, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_code = 'ROLE_ADMIN');

-- READ_ONLY -> ROLE_USER
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.role_id, p.privilege_id
FROM roles r
         JOIN privileges p ON p.privilege_code = 'READ_ONLY'
WHERE r.role_code = 'ROLE_USER'
  AND NOT EXISTS (
    SELECT 1 FROM role_privileges rp
    WHERE rp.role_id = r.role_id AND rp.privilege_id = p.privilege_id
);

-- ALL privileges -> ROLE_ADMIN
INSERT INTO role_privileges (role_id, privilege_id)
SELECT r.role_id, p.privilege_id
FROM roles r
         JOIN privileges p ON 1=1
WHERE r.role_code = 'ROLE_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM role_privileges rp
    WHERE rp.role_id = r.role_id AND rp.privilege_id = p.privilege_id
);

COMMIT;