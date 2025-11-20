package fit.iam_service.dtos.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeleteUserResult {
    private final String userId;
    private final String deletedBy;
    private final LocalDateTime deletedAt;
}
