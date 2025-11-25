package fit.test_order_service.mappers;

import fit.test_order_service.client.dtos.PatientMedicalRecordInternalResponse;
import fit.test_order_service.dtos.response.*;
import fit.test_order_service.entities.OrderComment;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.enums.CommentTargetType;
import fit.test_order_service.repositories.OrderCommentRepository;
import fit.test_order_service.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestOrderMapper {
    private final OrderCommentRepository orderCommentRepository;
    /**
     * Chuyển đổi từ PatientMedicalRecordInternalResponse DTO (nhận từ patient-service) sang TestOrder entity.
     * Bao gồm cả logic tính tuổi.
     */
    public TestOrder toEntity(PatientMedicalRecordInternalResponse patientData) {
        if (patientData == null) {
            return null;
        }

        TestOrder testOrder = new TestOrder();
        testOrder.setMedicalRecordId(patientData.medicalRecordId());
        testOrder.setMedicalRecordCode(patientData.medicalRecordCode());
        testOrder.setFullName(patientData.fullName());
        testOrder.setPhone(patientData.phone());
        if (patientData.dateOfBirth() != null) {
            LocalDate dob = patientData.dateOfBirth().toLocalDate();
            testOrder.setDateOfBirth(dob);
            int age = Period.between(dob, LocalDate.now()).getYears();
            testOrder.setAgeYearsSnapshot(age);
        } else {
            testOrder.setAgeYearsSnapshot(0);
        }
        testOrder.setGender(patientData.gender());
        testOrder.setAddress(patientData.address());
        testOrder.setEmail(patientData.email());

        return testOrder;
    }

    /**
     * Converts a TestOrder entity to a TestOrderResponse DTO.
     *
     * @param testOrder The TestOrder entity to convert.
     * @return A TestOrderResponse DTO.
     */
    public TestOrderResponse toResponse(TestOrder testOrder, TestTypeResponse testTypeResponse) {
        if (testOrder == null) {
            return null;
        }

        List<OrderComment> comments = orderCommentRepository
                .findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(
                        CommentTargetType.ORDER,
                        testOrder.getOrderId()
                );

        List<OrderCommentResponse> commentResponses = this.toCommentResponseList(comments);

        return TestOrderResponse.builder()
                .id(testOrder.getOrderId())
                .orderCode(testOrder.getOrderCode())
                .barcode(testOrder.getBarcode())
                .testType(testTypeResponse)
                .medicalRecordId(testOrder.getMedicalRecordId())
                .medicalRecordCode(testOrder.getMedicalRecordCode())
                .fullName(testOrder.getFullName())
                .age(testOrder.getAgeYearsSnapshot())
                .gender(testOrder.getGender())
                .phone(testOrder.getPhone())
                .address(testOrder.getAddress())
                .email(testOrder.getEmail())
                .dateOfBirth(testOrder.getDateOfBirth() != null ? testOrder.getDateOfBirth().toString() : null)
                .status(testOrder.getStatus())
                .createdAt(DateUtils.toVietnamTime(testOrder.getCreatedAt()))
                .createdBy(testOrder.getCreatedBy())
                .runAt(testOrder.getRunAt())
                .runBy(testOrder.getRunBy())
                .updatedAt(DateUtils.toVietnamTime(testOrder.getUpdatedAt()))
                .updatedBy(testOrder.getUpdatedBy())
                .reviewStatus(testOrder.getReviewStatus())
                .reviewMode(testOrder.getReviewMode())
                .reviewedAt(DateUtils.toVietnamTime(testOrder.getReviewedAt()))
                .reviewedBy(testOrder.getReviewedBy())
                .deletedAt(DateUtils.toVietnamTime(testOrder.getDeletedAt()))
                .deletedBy(testOrder.getDeletedBy())
                .results(toResultResponseList(testOrder.getResults()))
                .comments(commentResponses)
                .build();
    }

    /**
     * Converts a TestOrder entity to a TestOrderDetailResponse DTO.
     */
    public TestOrderDetailResponse toDetailResponse(TestOrder testOrder) {
        if (testOrder == null) {
            return null;
        }

        return TestOrderDetailResponse.builder()
                .id(testOrder.getOrderId())
                .orderCode(testOrder.getOrderCode())
                .medicalRecordId(testOrder.getMedicalRecordId())
                .medicalRecordCode(testOrder.getMedicalRecordCode())
                .fullName(testOrder.getFullName())
                .age(testOrder.getAgeYearsSnapshot())
                .gender(testOrder.getGender())
                .phone(testOrder.getPhone())
                .address(testOrder.getAddress())
                .email(testOrder.getEmail())
                .dateOfBirth(testOrder.getDateOfBirth() != null ? testOrder.getDateOfBirth().toString() : null)
                .status(testOrder.getStatus())
                .createdAt(testOrder.getCreatedAt())
                .createdBy(testOrder.getCreatedBy())
                .runAt(testOrder.getRunAt())
                .runBy(testOrder.getRunBy())
                .updatedAt(testOrder.getUpdatedAt())
                .updatedBy(testOrder.getUpdatedBy())
                .reviewStatus(testOrder.getReviewStatus())
                .reviewMode(testOrder.getReviewMode())
                .reviewedAt(testOrder.getReviewedAt())
                .reviewedBy(testOrder.getReviewedBy())
                .results(toResultResponseList(testOrder.getResults()))
                .build();
    }

    /**
     * Converts a TestResult entity to a TestResultResponse DTO.
     */
    public TestResultResponse toResultResponse(TestResult testResult) {
        if (testResult == null) {
            return null;
        }
        return TestResultResponse.builder()
                .id(testResult.getResultId())
                .analyteName(testResult.getAnalyteName())
                .testName(testResult.getAnalyteName())
                .testCode(testResult.getTestCode())
                .value(testResult.getValueText())
                .unit(testResult.getUnit())
                .referenceRange(testResult.getReferenceRange())
                .abnormalFlag(testResult.getAbnormalFlag())
                .measuredAt(testResult.getMeasuredAt())
                .entrySource(testResult.getEntrySource())
                .enteredBy(testResult.getEnteredBy())
                .enteredAt(testResult.getEnteredAt())
//                .flagCode(testResult.getFlagCode())
//                .flagSeverity(testResult.getFlagSeverity())
                .build();
    }

    /**
     * Converts a list of TestResult entities to a list of TestResultResponse DTOs.
     */
    public List<TestResultResponse> toResultResponseList(List<TestResult> testResults) {
        if (testResults == null || testResults.isEmpty()) {
            return Collections.emptyList();
        }
        return testResults.stream()
                .map(this::toResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts an OrderComment entity to an OrderCommentResponse DTO.
     */
    public OrderCommentResponse toCommentResponse(OrderComment orderComment) {
        if (orderComment == null) {
            return null;
        }
        List<OrderCommentResponse> replyResponses = null;
        List<OrderComment> replies = orderComment.getReplies();

        if (replies != null && !replies.isEmpty()) {
            replyResponses = replies.stream()
                    .map(this::toSimpleCommentResponse)
                    .collect(Collectors.toList());
        }

        return OrderCommentResponse.builder()
                .id(orderComment.getCommentId())
                .authorId(orderComment.getAuthorUserId())
                .content(orderComment.getContent())
                .createdAt(orderComment.getCreatedAt())
                .replies(replyResponses)
                .build();
    }

    /**
     * Converts a list of OrderComment entities to a list of OrderCommentResponse DTOs.
     */
    public List<OrderCommentResponse> toCommentResponseList(List<OrderComment> orderComments) {
        if (orderComments == null || orderComments.isEmpty()) {
            return Collections.emptyList();
        }
        return orderComments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a reply comment to a simplified OrderCommentResponse (no nested replies).
     */
    private OrderCommentResponse toSimpleCommentResponse(OrderComment comment) {
        if (comment == null) {
            return null;
        }

        return OrderCommentResponse.builder()
                .id(comment.getCommentId())
                .authorId(comment.getAuthorUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}