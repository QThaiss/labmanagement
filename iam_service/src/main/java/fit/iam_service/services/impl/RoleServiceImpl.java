package fit.iam_service.services.impl;

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.dtos.request.RoleListQuery;
import fit.iam_service.dtos.request.UpdateRoleRequest;
import fit.iam_service.dtos.response.CreateRoleResponse;
import fit.iam_service.dtos.response.DeleteRoleResponse;
import fit.iam_service.dtos.response.RoleListItem;
import fit.iam_service.dtos.response.UpdateRoleResponse;
import fit.iam_service.entities.AuditLog;
import fit.iam_service.entities.Privilege;
import fit.iam_service.entities.Role;
import fit.iam_service.enums.AuditAction;
import fit.iam_service.enums.PrivilegeCode;
import fit.iam_service.exceptions.AlreadyExistsException;
import fit.iam_service.exceptions.InvalidFormatException;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.mappers.RoleMapper;
import fit.iam_service.repositories.AuditLogRepository;
import fit.iam_service.repositories.PrivilegeRepository;
import fit.iam_service.repositories.RoleRepository;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.services.RoleService;
import fit.iam_service.utils.SecurityUtils;
import fit.iam_service.validators.RoleValidator;
import jakarta.persistence.criteria.Predicate;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final RoleValidator roleValidator;
    private final RoleMapper roleMapper;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;


    @Override
    public CreateRoleResponse createRole(CreateRoleRequest req, String actorId, String clientIp, String userAgent) {
        // validate request
        roleValidator.validateCreateRole(req);

        // check trùng roleName
        String roleName = req.getRoleName().trim();
        if (roleRepository.existsByRoleNameAndIsDeletedFalse(roleName)) {
            throw new AlreadyExistsException("Role name đã tồn tại");
        }

        String roleCode = req.getRoleCode().trim().toUpperCase(Locale.ROOT);

        // check trùng roleCode
        if (roleRepository.existsByRoleCode(roleCode)) {
            throw new AlreadyExistsException("Role code đã tồn tại");
        }

        // Nếu không truyền privileges → mặc định READ_ONLY
        List<String> codes = (req.getPrivilegeCodes() == null || req.getPrivilegeCodes().isEmpty())
                ? List.of("READ_ONLY")
                : req.getPrivilegeCodes();

        // Lấy list Privilege entity từ DB
        Set<Privilege> privileges = codes.stream()
                .map(code -> {
                    PrivilegeCode enumCode;
                    try {
                        enumCode = PrivilegeCode.valueOf(code.toUpperCase());
                    } catch (Exception e) {
                        throw new InvalidFormatException("Privilege không hợp lệ: " + code);
                    }

                    return privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(enumCode)
                            .orElseThrow(() -> new NotFoundException("Privilege không tồn tại: " + code));
                })
                .collect(Collectors.toSet());

        Set<String> newPrivilegeCodes = privileges.stream()
                .map(p -> p.getPrivilegeCode().name())
                .collect(Collectors.toSet());

        List<Role> allRoles = roleRepository.findAll();
        for (Role existingRole : allRoles) {
            Set<String> existingPrivCodes = existingRole.getRolePrivileges().stream()
                    .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                    .collect(Collectors.toSet());

            if (existingPrivCodes.equals(newPrivilegeCodes)) {
                throw new AlreadyExistsException("Role đã tồn tại với cùng bộ quyền (" + existingRole.getRoleName() + ")");
            }
        }

        // Map từ request -> entity
        Role role = roleMapper.toEntity(req, actorId);

        // Gán privileges
        role.replacePrivileges(privileges);

        // Lưu DB
        Role saved = roleRepository.save(role);

        // Ghi log
        AuditLog log = AuditLog.builder()
                .actorId(actorId)                       // người thực hiện
                .targetId(saved.getRoleId())            // role mới tạo
                .entity("ROLE")
                .action(AuditAction.CREATE_ROLE)
                .detailsJson("{\"after\":{" +
                        "\"roleCode\":\"" + saved.getRoleCode() + "\"," +
                        "\"roleName\":\"" + saved.getRoleName() + "\"," +
                        "\"privilegeCodes\":" + saved.getRolePrivileges().stream()
                        .map(rp -> "\"" + rp.getPrivilege().getPrivilegeCode().name() + "\"")
                        .collect(Collectors.joining(",", "[", "]")) +
                        "}}")
                .ip(clientIp)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(log);

        // Trả về response
        return roleMapper.toResponse(saved);
    }

    // Escape ký tự " và null-safe
    private static String j(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    // List<String> → JSON array
    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        return list.stream()
                .map(v -> "\"" + j(v) + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String buildDiffJson(
            String beforeName,
            String beforeDesc,
            List<String> beforePrivileges,
            Role after // dùng role sau update
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');

        // BEFORE
        sb.append("\"before\":{")
                .append("\"roleName\":\"").append(j(beforeName)).append("\",")
                .append("\"roleDescription\":\"").append(j(beforeDesc)).append("\",")
                .append("\"privilegeCodes\":").append(toJsonArray(beforePrivileges))
                .append("},");

        // AFTER
        sb.append("\"after\":{")
                .append("\"roleId\":\"").append(j(after.getRoleId())).append("\",")
                .append("\"roleName\":\"").append(j(after.getRoleName())).append("\",")
                .append("\"roleDescription\":\"").append(j(after.getRoleDescription())).append("\",")
                .append("\"privilegeCodes\":").append(toJsonArray(
                        after.getRolePrivileges().stream()
                                .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                                .toList()
                ))
                .append("}");

        sb.append('}');
        return sb.toString();
    }

    @Override
    @Transactional
    public UpdateRoleResponse updateRole(
            String roleId,
            UpdateRoleRequest request,
            String actorId,
            String ip,
            String userAgent
    ) {
        //  Load role
        Role role = roleRepository.findByRoleIdAndIsDeletedFalse(roleId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy role"));

        // Policy check
        if (role.isSystem()) {
            throw new ForbiddenException("Bạn không có quyền cập nhật role hệ thống");
        }


        String beforeName = role.getRoleName();
        String beforeDesc = role.getRoleDescription();
        List<String> beforePrivileges = role.getRolePrivileges().stream()
                .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                .toList();

        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            String newName = request.getRoleName().trim();
            if (!newName.equalsIgnoreCase(beforeName)) { // Check nếu tên thật sự thay đổi
                if (roleRepository.existsByRoleNameAndIsDeletedFalse(newName)) {
                    throw new AlreadyExistsException("Role name đã tồn tại");
                }
            }
        }

        Set<Privilege> newPrivileges = null;
        List<String> codes = request.getPrivilegeCodes();

        if (codes != null) {
            if (codes.isEmpty()) {
                newPrivileges = Collections.emptySet();
            } else {
                newPrivileges = codes.stream()
                        .map(code -> {
                            PrivilegeCode enumCode;
                            try {
                                enumCode = PrivilegeCode.valueOf(code.toUpperCase());
                            } catch (Exception e) {
                                throw new InvalidFormatException("Privilege không hợp lệ: " + code);
                            }
                            return privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(enumCode)
                                    .orElseThrow(() -> new NotFoundException("Privilege không tồn tại: " + code));
                        })
                        .collect(Collectors.toSet());
            }

            Set<String> newPrivilegeCodes = newPrivileges.stream()
                    .map(p -> p.getPrivilegeCode().name())
                    .collect(Collectors.toSet());

            List<Role> otherRoles = roleRepository.findAll().stream()
                    .filter(r -> !r.getRoleId().equals(roleId))
                    .collect(Collectors.toList());

            for (Role existingRole : otherRoles) {
                Set<String> existingPrivCodes = existingRole.getRolePrivileges().stream()
                        .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                        .collect(Collectors.toSet());

                if (existingPrivCodes.equals(newPrivilegeCodes)) {
                    throw new AlreadyExistsException("Role đã tồn tại với cùng bộ quyền (" + existingRole.getRoleName() + ")");
                }
            }
        }


        roleMapper.updateEntity(request, role, actorId, newPrivileges);
        Role updated = roleRepository.saveAndFlush(role);


        String detailsJson = buildDiffJson(beforeName, beforeDesc, beforePrivileges, updated);

        // Audit Log
        AuditLog log = AuditLog.builder()
                .actorId(actorId)
                .targetId(updated.getRoleId())
                .entity("ROLE")
                .action(AuditAction.UPDATE_ROLE)
                .detailsJson(detailsJson)
                .ip(ip)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(log);

        // Response
        return roleMapper.toUpdateResponse(updated);
    }


    @Override
    @Transactional
    public DeleteRoleResponse deleteRole(String roleId) {
        //  Tìm role chưa bị xóa (do @Where lọc sẵn is_deleted=false)
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy role"));

        //  Kiểm tra role hệ thống
        if (role.isSystem()) {
            throw new RuntimeException("Không thể xóa role hệ thống");
        }

        //  Kiểm tra role có đang được user sử dụng
        boolean isUsed = userRepository.existsByRole_RoleId(roleId);
        if (isUsed) {
            throw new RuntimeException("Role đang được người dùng sử dụng, không thể xóa");
        }
        String actor = SecurityUtils.getCurrentUserId();
        System.out.println("Actor ID: " + actor);
        //  Gán deletedBy và deletedAt (Hibernate sẽ tự set isDeleted=true nhờ @SQLDelete)
        role.setDeletedBy(actor);
        role.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        // Soft Delete role
        role.setDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        role.setDeletedBy(SecurityUtils.getCurrentUserId());
        roleRepository.save(role);


        String detailsJson = String.format(
                "{\"hard\":false,\"roleCode\":\"%s\",\"roleName\":\"%s\"}",
                role.getRoleCode(),
                role.getRoleName()
        );



        AuditLog log = AuditLog.builder()
                .actorId(actor)
                .targetId(role.getRoleId())
                .entity("ROLE")
                .action(AuditAction.DELETE_ROLE)
                .detailsJson(detailsJson)
                .createdAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();

        auditLogRepository.save(log);

        //  Trả kết quả phản hồi
        return DeleteRoleResponse.builder()
                .roleId(role.getRoleId())
                .deleted(true)
                .hard(false)
                .deletedAt(role.getDeletedAt())
                .deletedBy(SecurityUtils.getCurrentUserId())
                .build();
    }
    @Override
    public PageResult<RoleListItem> list(RoleListQuery q) {
        // 1. Thiết lập mặc định
        int page = (q.page() == null || q.page() < 0) ? 0 : q.page();
        int size = (q.size() == null || q.size() <= 0) ? 20 : Math.min(q.size(), 100);
        String sortBy = (q.sortBy() == null || q.sortBy().isBlank()) ? "roleName" : q.sortBy();
        Sort.Direction dir = "desc".equalsIgnoreCase(q.sortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // 2. Giới hạn whitelist trường sort
        sortBy = switch (sortBy) {
            case "roleCode", "roleName", "createdAt", "updatedAt" -> sortBy;
            default -> "roleName";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        // 3. Build Specification động
        Specification<Role> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // Ẩn role đã bị soft-delete
            ps.add(cb.isFalse(root.get("isDeleted")));

            // full-text search
            if (q.q() != null && !q.q().isBlank()) {
                String kw = "%" + q.q().trim().toLowerCase() + "%";
                ps.add(
                        cb.or(
                                cb.like(cb.lower(root.get("roleName")), kw),
                                cb.like(cb.lower(root.get("roleCode")), kw),
                                cb.like(cb.lower(root.get("roleDescription")), kw)
                        )
                );
            }

            // lọc roleCode chính xác
            if (q.roleCode() != null && !q.roleCode().isBlank()) {
                ps.add(cb.equal(cb.lower(root.get("roleCode")), q.roleCode().trim().toLowerCase()));
            }

            // lọc role hệ thống
            if (q.isSystem() != null) {
                ps.add(cb.equal(root.get("isSystem"), q.isSystem()));
            }

            // khoảng thời gian tạo
            if (q.createdFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("createdAt"), q.createdFrom()));
            }
            if (q.createdTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("createdAt"), q.createdTo()));
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };

        // 4. Query DB
        Page<Role> pageData = roleRepository.findAll(spec, pageable);

        // 5. Map sang DTO
        Page<RoleListItem> mapped = pageData.map(r -> new RoleListItem(
                r.getRoleId(),
                r.getRoleCode(),
                r.getRoleName(),
                r.getRoleDescription(),
                r.isSystem(),
                r.getRolePrivileges().stream()
                        .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                        .toList(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        ));

        // 6. Trả về PageResult
        return PageResult.of(mapped);
    }
}
