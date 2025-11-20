/*
 * @ (#) RoleValidator.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.validators;
/*
 * @description: Validator cho Role khi create/update
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.enums.PrivilegeCode;

import fit.iam_service.exceptions.InvalidFormatException;
import fit.iam_service.exceptions.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class RoleValidator {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    public void validateCreateRole(CreateRoleRequest req) {
        // validate roleName
        if (req.getRoleName() == null || req.getRoleName().trim().isEmpty()) {
            throw new NotFoundException("roleName là bắt buộc");
        }
        if (req.getRoleName().length() > 128) {
            throw new InvalidFormatException("roleName không được quá 128 ký tự");
        }
        // validate roleCode
        if (req.getRoleCode() == null || req.getRoleCode().trim().isEmpty()) {
            throw new IllegalArgumentException("roleCode là bắt buộc");
        }
        if (req.getRoleCode().length() > 64) {
            throw new InvalidFormatException("roleCode không được quá 64 ký tự");
        }
        if (!ROLE_CODE_PATTERN.matcher(req.getRoleCode()).matches()) {
            throw new InvalidFormatException("roleCode chỉ cho phép ký tự IN HOA (A-Z), số (0-9) và gạch dưới (_)");
        }
        // validate roleDescription
        if (req.getRoleDescription() != null && req.getRoleDescription().length() > 255) {
            throw new InvalidFormatException("roleDescription không được quá 255 ký tự");
        }
        // validate privilegeCodes nếu có
        List<String> codes = req.getPrivilegeCodes();
        if (codes != null) {
            for (int i = 0; i < codes.size(); i++) {
                String code = codes.get(i);
                try {
                    PrivilegeCode.valueOf(code.toUpperCase());
                } catch (Exception e) {
                    throw new InvalidFormatException("PrivilegeCodes[" + i + "] không hợp lệ: " + code);
                }
            }
        }
    }
}