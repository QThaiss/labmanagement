package fit.iam_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UpdateRoleResponse {
    private String roleId;

    private String roleCode;

    private String roleName;

    private String roleDescription;

    private boolean isSystem;

    private List<String> privilegeCodes;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
