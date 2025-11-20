package fit.iam_service.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleRequest {
    @Size(max = 128, message = "Role name must be ≤ 128 characters")
    private String roleName;

    @Size(max = 255, message = "Role description must be ≤ 255 characters")
    private String roleDescription;

    private List<String> privilegeCodes;
}
