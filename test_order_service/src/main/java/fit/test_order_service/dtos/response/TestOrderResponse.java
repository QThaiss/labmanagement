package fit.test_order_service.dtos.response;

import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewMode;
import fit.test_order_service.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderResponse {
    private String id;
    private String orderCode;
    private String barcode;
    String medicalRecordId;
    String medicalRecordCode;
    TestTypeResponse testType;

    String fullName;
    private Integer age;
    private Gender gender;
    private String phone;
    private String address;
    private String email;
    private String dateOfBirth;

    private OrderStatus status;
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime runAt;
    private String runBy;

    private LocalDateTime updatedAt;
    private String updatedBy;

    private ReviewStatus reviewStatus;
    private ReviewMode reviewMode;
    private LocalDateTime reviewedAt;
    private String reviewedBy;

    private LocalDateTime deletedAt;
    private String deletedBy;

    private List<TestResultResponse> results;
    private List<OrderCommentResponse> comments;
}