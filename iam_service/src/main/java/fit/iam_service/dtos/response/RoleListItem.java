package fit.iam_service.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

public record RoleListItem(
        String roleId,
        String roleCode,
        String roleName,
        String roleDescription,
        boolean isSystem,
        List<String> privilegeCodes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
