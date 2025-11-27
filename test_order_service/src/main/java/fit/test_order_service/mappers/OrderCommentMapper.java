package fit.test_order_service.mappers;

import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.request.AddCommentRequest;
import fit.test_order_service.dtos.request.DeleteOrderCommentRequest;
import fit.test_order_service.dtos.request.UpdateOrderCommentRequest;
// Cập nhật imports để chỉ giữ lại DTO chung
import fit.test_order_service.dtos.response.OrderCommentResponse;
import fit.test_order_service.dtos.response.CommentTargetResponse;
import fit.test_order_service.dtos.response.CreatedBySummary;
import fit.test_order_service.entities.*;
import fit.test_order_service.enums.CommentTargetType;
import fit.test_order_service.enums.EventType;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.exceptions.InvalidCommentContentException;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j; // Thêm lại import cho @Slf4j nếu cần thiết

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCommentMapper {

    private final IamFeignClient iamFeignClient;

    public OrderComment toEntity(AddCommentRequest request) {
        if (request == null) return null;

        CommentTargetType targetType;
        try {
            targetType = CommentTargetType.valueOf(request.getTargetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid target type: " + request.getTargetType());
        }

        if (request.getTargetId() == null || request.getTargetId().trim().isEmpty()) {
            throw new BadRequestException("Target ID cannot be null or empty.");
        }

        return OrderComment.builder()
                .targetType(targetType)
                .targetId(request.getTargetId().trim())
                .authorUserId(SecurityUtils.getCurrentUserId())
                .content(request.getContent().trim())
                .edited(false)
                .build();
    }

    /**
     * Thay thế cho toCreateResponse(AddCommentResponse)
     * Chuyển đổi sang OrderCommentResponse cho phản hồi tạo mới, bao gồm full name.
     */
    public OrderCommentResponse toCreateResponse(OrderComment comment) {
        if (comment == null) return null;

        CommentTargetResponse target = null;
        if (comment.getTargetType() == CommentTargetType.ORDER) {
            target = CommentTargetResponse.builder()
                    .type("ORDER")
                    .testOrderId(comment.getTargetId())
                    .build();
        } else if (comment.getTargetType() == CommentTargetType.RESULT) {
            target = CommentTargetResponse.builder()
                    .type("RESULT")
                    .testOrderId(null) // Sẽ được cập nhật ở service nếu cần
                    .resultId(comment.getTargetId())
                    .build();
        }


        // Lấy thông tin user từ IAM - Đảm bảo lấy full name
        CreatedBySummary createdBy = CreatedBySummary.builder()
                .userId(comment.getAuthorUserId())
                .fullName("Unknown User")
                .build();

        try {
            var response = iamFeignClient.getUserById(comment.getAuthorUserId());
            if (response != null && response.getData() != null) {
                UserInternalResponse user = response.getData();
                createdBy.setFullName(user.fullName());
            }
        } catch (Exception e) {
            // Bỏ qua lỗi feign, trả về Unknown User
            log.error("Error fetching user {} info from IAM for comment creation: {}", comment.getAuthorUserId(), e.getMessage());
        }

        return OrderCommentResponse.builder()
                .id(comment.getCommentId()) // Đổi commentId thành id
                .authorId(comment.getAuthorUserId())
                .target(target)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .createdBy(createdBy) // Đã có full name
                .edited(comment.isEdited())
                .editCount(comment.getEditCount())
                .updatedBy(comment.getUpdatedBy())
                .updatedAt(comment.getUpdatedAt())
                .replies(null)
                .build();
    }

    public void updateEntity(OrderComment comment, UpdateOrderCommentRequest request) {
        if (comment == null || request == null)
            throw new InvalidCommentContentException("Comment or request cannot be null.");

        String newContent = request.getNewContent().trim();
        if (newContent.isEmpty()) {
            throw new InvalidCommentContentException("New comment content cannot be empty.");
        }

        if (newContent.equals(comment.getContent())) {
            throw new InvalidCommentContentException("New content must be different from the old one.");
        }

        comment.setContent(newContent);
        comment.setUpdatedBy(SecurityUtils.getCurrentUserId());
        comment.setEdited(true);
        comment.setEditCount(comment.getEditCount() + 1);
        comment.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Thay thế cho toUpdateResponse(UpdateOrderCommentResponse)
     * Chuyển đổi sang OrderCommentResponse cho phản hồi cập nhật.
     */
    public OrderCommentResponse toUpdateResponse(OrderComment comment) {
        if (comment == null) return null;

        return OrderCommentResponse.builder()
                .id(comment.getCommentId())
                .authorId(comment.getAuthorUserId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                // Thông tin cập nhật
                .edited(comment.isEdited())
                .editCount(comment.getEditCount())
                .updatedBy(comment.getUpdatedBy())
                .updatedAt(comment.getUpdatedAt())
                // Các trường khác được đặt là null/default
                .target(null)
                .createdBy(null)
                .replies(null)
                .build();
    }

    public OrderCommentEditLog toEditLog(OrderComment comment, UpdateOrderCommentRequest request) {
        // Phương thức này được gọi TRƯỚC khi 'comment' được update
        // nên comment.getContent() là beforeContent.
        return OrderCommentEditLog.builder()
                .commentId(comment.getCommentId())
                .targetId(comment.getTargetId()) // Đã sửa
                .targetType(comment.getTargetType().name()) // Đã sửa
                .editorUserId(SecurityUtils.getCurrentUserId())
                .beforeContent(comment.getContent()) // Nội dung cũ
                .afterContent(request.getNewContent()) // Nội dung mới
                .note("Modified by user " + SecurityUtils.getCurrentUserId())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    /**
     * Tạo event log cho việc tạo comment.
     * @param comment Comment vừa được tạo
     * @param orderIdForLog Order ID liên quan (kể cả khi comment gắn vào result)
     */
    public OrderEventLog toEventLogForCommentCreate(OrderComment comment, String orderIdForLog) {
        return OrderEventLog.builder()
                .orderId(orderIdForLog) // Sử dụng orderId đã được service xác định
                .actorUserId(comment.getAuthorUserId())
                .eventType(EventType.ADD_COMMENT)
                .details(String.format(
                        "User [%s] added new comment [%s] to %s [%s]. Content: \"%s\"",
                        comment.getAuthorUserId(),
                        comment.getCommentId(),
                        comment.getTargetType().name(),
                        comment.getTargetId(),
                        comment.getContent()
                ))
                .afterJson("{\"commentId\":\"" + comment.getCommentId() + "\",\"content\":\"" + comment.getContent() + "\"}")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    /**
     * Tạo event log cho việc cập nhật comment.
     * @param comment Comment (đã được cập nhật)
     * @param oldContent Nội dung text cũ (trước khi cập nhật)
     * @param request Request dùng để cập nhật
     * @param orderIdForLog Order ID liên quan (kể cả khi comment gắn vào result)
     */
    public OrderEventLog toEventLogForCommentUpdate(OrderComment comment, String oldContent, UpdateOrderCommentRequest request, String orderIdForLog) {
        return OrderEventLog.builder()
                .orderId(orderIdForLog) // Sử dụng orderId đã được service xác định
                .actorUserId(SecurityUtils.getCurrentUserId())
                .eventType(EventType.UPDATE_COMMENT)
                .details(String.format(
                        "User [%s] modified comment [%s] on %s [%s].",
                        SecurityUtils.getCurrentUserId(),
                        comment.getCommentId(),
                        comment.getTargetType().name(),
                        comment.getTargetId()
                ))
                .beforeJson("{\"oldContent\":\"" + oldContent + "\"}") // Sửa lỗi logic: Dùng oldContent
                .afterJson("{\"newContent\":\"" + request.getNewContent() + "\"}")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    /**
     * Tạo event log cho việc xóa comment.
     * @param comment Comment vừa được xóa
     * @param request Request dùng để xóa
     * @param orderIdForLog Order ID liên quan (kể cả khi comment gắn vào result)
     */
    public OrderEventLog toEventLogForCommentDelete(OrderComment comment, DeleteOrderCommentRequest request, String orderIdForLog) {
        return OrderEventLog.builder()
                .orderId(orderIdForLog) // Sử dụng orderId đã được service xác định
                .actorUserId(SecurityUtils.getCurrentUserId())
                .eventType(EventType.DELETE_COMMENT)
                .details(String.format(
                        "User [%s] deleted comment [%s] on %s [%s]. Reason: %s",
                        SecurityUtils.getCurrentUserId(),
                        comment.getCommentId(),
                        comment.getTargetType().name(),
                        comment.getTargetId(),
                        request.getDeleteReason() != null ? request.getDeleteReason() : "N/A"
                ))
                .beforeJson("{\"commentId\":\"" + comment.getCommentId() + "\",\"content\":\"" + comment.getContent() + "\"}")
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    /**
     * Chuyển đổi Entity sang DTO (Cấp 1 - bao gồm replies)
     */
    public OrderCommentResponse toCommentResponse(OrderComment comment) {
        if (comment == null) {
            return null;
        }

        // 1. Map các replies (cấp 2)
        List<OrderCommentResponse> replyResponses = null;
        List<OrderComment> replies = comment.getReplies(); // @Transactional sẽ lo việc lazy-load

        if (replies != null && !replies.isEmpty()) {
            replyResponses = replies.stream()
                    .map(this::toSimpleCommentResponse) // Dùng hàm map đơn giản (cấp 2)
                    .collect(Collectors.toList());
        }

        // --- Target Mapping ---
        CommentTargetResponse target = null;
        if (comment.getTargetType() == CommentTargetType.ORDER) {
            target = CommentTargetResponse.builder()
                    .type("ORDER")
                    .testOrderId(comment.getTargetId())
                    .build();
        } else if (comment.getTargetType() == CommentTargetType.RESULT) {
            target = CommentTargetResponse.builder()
                    .type("RESULT")
                    .testOrderId(null)
                    .resultId(comment.getTargetId())
                    .build();
        }
        // ----------------------

        // 2. Map comment cha (cấp 1) và gán list replies
        return OrderCommentResponse.builder()
                .id(comment.getCommentId())
                .authorId(comment.getAuthorUserId()) // Giữ nguyên String
                .target(target)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .edited(comment.isEdited())
                .editCount(comment.getEditCount())
                .updatedBy(comment.getUpdatedBy())
                .updatedAt(comment.getUpdatedAt())
                .replies(replyResponses) // Gán danh sách replies
                .createdBy(null) // Sẽ được service layer bổ sung (enrichment)
                .build();
    }

    /**
     * Chuyển đổi Entity sang DTO (Cấp 2 - không lồng)
     * Dùng cho replies.
     */
    private OrderCommentResponse toSimpleCommentResponse(OrderComment comment) {
        return OrderCommentResponse.builder()
                .id(comment.getCommentId())
                .authorId(comment.getAuthorUserId()) // Giữ nguyên String
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .edited(comment.isEdited())
                .editCount(comment.getEditCount())
                .updatedBy(comment.getUpdatedBy())
                .updatedAt(comment.getUpdatedAt())
                // Cấp 2 không có replies, target, hay createdBy
                .replies(null)
                .target(null)
                .createdBy(null)
                .build();
    }
}