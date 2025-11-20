package fit.iam_service.dtos.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoleResponse {
    private String roleId;

    private String roleCode;

    private String roleName;

    private String roleDescription;

    private boolean isSystem;

    private List<String> privilegeCodes;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
