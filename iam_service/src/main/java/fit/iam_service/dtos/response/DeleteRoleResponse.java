package fit.iam_service.dtos.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DeleteRoleResponse {
    private String roleId;
    private boolean deleted;
    private boolean hard;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
