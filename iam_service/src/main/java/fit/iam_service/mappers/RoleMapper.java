package fit.iam_service.mappers;

import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.dtos.request.UpdateRoleRequest;
import fit.iam_service.dtos.response.CreateRoleResponse;
import fit.iam_service.dtos.response.UpdateRoleResponse;
import fit.iam_service.entities.Privilege;
import fit.iam_service.entities.Role;
import fit.iam_service.entities.RolePrivilege;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    // Map từ Entity → Response
    public CreateRoleResponse toResponse(Role role) {
        return CreateRoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .isSystem(role.isSystem())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .privilegeCodes(
                        role.getRolePrivileges().stream()
                                .map(RolePrivilege::getPrivilege)
                                .map(p -> p.getPrivilegeCode().name())
                                .collect(Collectors.toList())
                )
                .build();
    }

    // Map từ Request → Entity (chưa gán privileges)
    public Role toEntity(CreateRoleRequest req, String actorId) {
        return Role.builder()
                .roleCode(req.getRoleCode().trim().toUpperCase(Locale.ROOT))
                .roleName(req.getRoleName())
                .roleDescription(req.getRoleDescription())
                .createdBy(actorId)
                .build();
    }
    public void updateEntity(UpdateRoleRequest req, Role role, String actorId, Set<Privilege> privileges) {
        if (req.getRoleName() != null) {
            role.setRoleName(req.getRoleName().trim());
        }

        if (req.getRoleDescription() != null && !req.getRoleDescription().trim().isEmpty()) {
            role.setRoleDescription(req.getRoleDescription().trim());
        }
        role.setUpdatedBy(actorId);
        role.setUpdatedAt(LocalDateTime.now());

        // Nếu request có gửi privileges thì replace
        if (privileges != null) {
            if (role.getRolePrivileges() == null) {
                role.setRolePrivileges(new HashSet<>()); // đảm bảo non-null
            }
            role.replacePrivileges(privileges);
        }

    }

    public UpdateRoleResponse toUpdateResponse(Role role) {
        return UpdateRoleResponse.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleDescription(role.getRoleDescription())
                .isSystem(role.isSystem())
                .updatedAt(role.getUpdatedAt())
                .updatedBy(role.getUpdatedBy())
                .privilegeCodes(
                        Optional.ofNullable(role.getRolePrivileges())
                                .orElse(Set.of())
                                .stream()
                                .filter(rp -> rp != null && rp.getPrivilege() != null)
                                .map(rp -> rp.getPrivilege().getPrivilegeCode().name())
                                .toList()
                )

                .build();
    }

}
