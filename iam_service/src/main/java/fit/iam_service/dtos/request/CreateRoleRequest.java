package fit.iam_service.dtos.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoleRequest {

    @NotBlank(message = "roleName là bắt buộc")
    @Size(max = 128, message = "roleName không được quá 128 ký tự")
    private String roleName;

    @NotBlank(message = "roleCode là bắt buộc")
    @Size(max = 64, message = "roleCode không được quá 64 ký tự")
    private String roleCode;

    @Size(max = 255, message = "roleDescription không được quá 255 ký tự")
    private String roleDescription;

    // Optional – nếu null hoặc empty thì default READ_ONLY
    private List<String> privilegeCodes;
}
