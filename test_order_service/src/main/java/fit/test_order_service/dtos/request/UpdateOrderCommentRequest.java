package fit.test_order_service.dtos.request;

import jakarta.validation.constraints.NotBlank;


import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderCommentRequest {
    @NotBlank(message = "New comment content cannot be empty.")
    private String newContent;

}
