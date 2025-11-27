package fit.test_order_service.services.impl;

import fit.test_order_service.dtos.event.SystemEvent;
import fit.test_order_service.dtos.request.AddCommentRequest;
import fit.test_order_service.dtos.request.DeleteOrderCommentRequest;
import fit.test_order_service.dtos.request.ReplyCommentRequest;
import fit.test_order_service.dtos.request.UpdateOrderCommentRequest;
import fit.test_order_service.dtos.response.AddCommentResponse;
import fit.test_order_service.dtos.response.DeleteOrderCommentResponse;
import fit.test_order_service.dtos.response.OrderCommentResponse;
import fit.test_order_service.dtos.response.UpdateOrderCommentResponse;
import fit.test_order_service.entities.OrderComment;
import fit.test_order_service.entities.OrderCommentEditLog;
import fit.test_order_service.entities.OrderEventLog;
import fit.test_order_service.entities.TestResult; // Giả định import
import fit.test_order_service.enums.CommentTargetType;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.exceptions.UnauthorizedException;
import fit.test_order_service.exceptions.TooManyRequestsException;
import fit.test_order_service.exceptions.DuplicateCommentException;
import fit.test_order_service.mappers.OrderCommentMapper;
import fit.test_order_service.repositories.*;
import fit.test_order_service.services.EventLogPublisher;
import fit.test_order_service.services.OrderCommentService;
import fit.test_order_service.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderCommentServiceImpl implements OrderCommentService {

    private final OrderCommentRepository orderCommentRepository;
    private final OrderCommentMapper orderCommentMapper;
    private final OrderCommentEditLogRepository editLogRepository;
    private final OrderEventLogRepository ordereventLogRepository;
    private final TestOrderRepository testOrderRepository;
    private final TestResultRepository testResultRepository;
    private final EventLogPublisher eventLogPublisher;

    /**
     * Tạo mới comment cho TestOrder hoặc TestResult
     */
    @Override
    @Transactional
    public OrderCommentResponse addComment(AddCommentRequest request) {

        // Validate dữ liệu (mapper cũng validate, nhưng service nên validate trước)
        CommentTargetType targetType;
        try {
            targetType = CommentTargetType.valueOf(request.getTargetType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid target type: " + request.getTargetType());
        }
        if (request.getTargetId() == null || request.getTargetId().trim().isEmpty()) {
            throw new BadRequestException("Target ID cannot be null or empty.");
        }

        // Kiểm tra Order hoặc Result tồn tại
        if (targetType == CommentTargetType.ORDER) {
            boolean exists = testOrderRepository.existsByOrderId(request.getTargetId());
            if (!exists) {
                throw new NotFoundException("TestOrder with ID " + request.getTargetId() + " does not exist.");
            }
        } else if (targetType == CommentTargetType.RESULT) {
            boolean exists = testResultRepository.existsByResultId(request.getTargetId());
            if (!exists) {
                throw new NotFoundException("TestResult with ID " + request.getTargetId() + " does not exist.");
            }
        }

        // Lấy user ID từ Security Context
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Cannot create comment without a logged-in user.");
        }

        // Anti-spam: Rate limit + Deduplication (gộp logic)
        int MAX_PER_MINUTE = 5;
        LocalDateTime oneMinuteAgo = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1);
        String targetId = request.getTargetId();

        int recentCount = orderCommentRepository
                .countByAuthorUserIdAndTargetIdAndCreatedAtAfter(currentUserId, targetId, oneMinuteAgo);
        if (recentCount >= MAX_PER_MINUTE) {
            throw new TooManyRequestsException("You are commenting too frequently on this target.");
        }

        LocalDateTime thirtySecondsAgo = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(30);
        boolean duplicate = orderCommentRepository
                .existsByAuthorUserIdAndTargetIdAndContentAndCreatedAtAfter(
                        currentUserId,
                        targetId,
                        request.getContent().trim(),
                        thirtySecondsAgo);
        if (duplicate) {
            throw new DuplicateCommentException("Duplicate comment detected within 30 seconds for this target.");
        }

        // Map DTO -> Entity
        OrderComment comment = orderCommentMapper.toEntity(request);
        comment.setAuthorUserId(currentUserId);
        comment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Lưu xuống DB
        OrderComment savedComment = orderCommentRepository.save(comment);

        // Ghi log
        // Phải lấy orderId kể cả khi comment gắn vào result
        String orderIdForLog = getOrderIdForLog(savedComment);
        OrderEventLog log = orderCommentMapper.toEventLogForCommentCreate(savedComment, orderIdForLog);
        ordereventLogRepository.save(log);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00005")
                .action("Add Comment")
                .message("Added comment to order " + request.getTargetId())
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("commentId", savedComment.getCommentId(), "content", request.getContent()))
                .build());

        // Map Entity -> Response (có fullName từ IAM)
        return orderCommentMapper.toCreateResponse(savedComment);
    }

    @Override
    @Transactional
    public OrderCommentResponse replyToComment(String parentCommentId, ReplyCommentRequest request) {
        // Lấy user ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Cannot create reply without a logged-in user.");
        }

        // 1. Tìm comment cha
        OrderComment parentComment = orderCommentRepository.findByCommentIdAndDeletedAtIsNull(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found or has been deleted."));

        // 2. LOGIC CHẶN 2 CẤP
        if (parentComment.getParentId() != null) {
            throw new BadRequestException("Cannot reply to a reply. Only 2 levels of comments are allowed.");
        }

        // 3. Anti-spam (Kiểm tra trên target gốc của cha)
        int MAX_PER_MINUTE = 5;
        LocalDateTime oneMinuteAgo = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1);
        String originalTargetId = parentComment.getTargetId();

        int recentCount = orderCommentRepository
                .countByAuthorUserIdAndTargetIdAndCreatedAtAfter(currentUserId, originalTargetId, oneMinuteAgo);
        if (recentCount >= MAX_PER_MINUTE) {
            throw new TooManyRequestsException("You are commenting too frequently on this target.");
        }

        LocalDateTime thirtySecondsAgo = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(30);
        boolean duplicate = orderCommentRepository
                .existsByAuthorUserIdAndTargetIdAndContentAndCreatedAtAfter(
                        currentUserId,
                        originalTargetId,
                        request.getContent().trim(),
                        thirtySecondsAgo);
        if (duplicate) {
            throw new DuplicateCommentException("Duplicate comment detected within 30 seconds for this target.");
        }

        // 4. Tạo entity cho comment reply (cấp 2)
        OrderComment reply = OrderComment.builder()
                .targetType(parentComment.getTargetType())
                .targetId(parentComment.getTargetId())
                .parentId(parentComment.getCommentId())
                .authorUserId(currentUserId)
                .content(request.getContent().trim())
                .edited(false)
                .build();

        // 5. Lưu xuống DB
        OrderComment savedReply = orderCommentRepository.save(reply);

        // 6. Ghi log
        String orderIdForLog = getOrderIdForLog(savedReply);
        OrderEventLog log = orderCommentMapper.toEventLogForCommentCreate(savedReply, orderIdForLog);
        ordereventLogRepository.save(log);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00005")
                .action("Reply Comment")
                .message("Reply to comment " + parentCommentId)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("commentId", savedReply.getCommentId(), "content", request.getContent()))
                .build());

        // 7. --- THAY ĐỔI LOGIC TRẢ VỀ ---
        // Trả về comment Cha (parentComment)
        // Mapper (toCommentResponse) sẽ tự động load danh sách replies (bao gồm cả reply vừa tạo)
        return orderCommentMapper.toCommentResponse(parentComment);
    }

    @Transactional
    @Override
    public OrderCommentResponse modifyComment(String commentId, UpdateOrderCommentRequest request) {

        // Tìm comment
        OrderComment comment = orderCommentRepository.findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found or has been deleted."));

        // Sửa lỗi logic: Lưu lại nội dung cũ TRƯỚC KHI update
        final String oldContent = comment.getContent();

        // Tạo log chỉnh sửa (OrderCommentEditLog)
        // (Phải gọi trước khi updateEntity)
        OrderCommentEditLog log = orderCommentMapper.toEditLog(comment, request);
        editLogRepository.save(log);

        // Áp dụng cập nhật vào entity (qua mapper)
        orderCommentMapper.updateEntity(comment, request);
        orderCommentRepository.save(comment);

        // Ghi event log (OrderEventLog)
        // (Phải gọi sau khi updateEntity, và truyền oldContent vào)
        String orderIdForLog = getOrderIdForLog(comment);
        OrderEventLog eventLog = orderCommentMapper.toEventLogForCommentUpdate(comment, oldContent, request, orderIdForLog);
        ordereventLogRepository.save(eventLog);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00006")
                .action("Modify Comment")
                .message("Modified comment " + commentId)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("commentId", commentId))
                .build());

        // Trả response
        return orderCommentMapper.toUpdateResponse(comment);
    }

    @Transactional
    @Override
    public DeleteOrderCommentResponse deleteComment(String commentId, DeleteOrderCommentRequest request) {

        // Tìm comment tồn tại và chưa xóa
        OrderComment comment = orderCommentRepository.findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found or already deleted."));

        // Áp dụng xóa mềm (soft delete)
        comment.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
        comment.setDeletedBy(SecurityUtils.getCurrentUserId());
        comment.setDeleteReason(request.getDeleteReason());
        orderCommentRepository.save(comment);

        // Ghi event log
        String orderIdForLog = getOrderIdForLog(comment);
        OrderEventLog log = orderCommentMapper.toEventLogForCommentDelete(comment, request, orderIdForLog);
        ordereventLogRepository.save(log);

        eventLogPublisher.publishEvent(SystemEvent.builder()
                .eventCode("E_00007")
                .action("Delete Comment")
                .message("Deleted comment " + commentId)
                .sourceService("TEST_ORDER_SERVICE")
                .operator(SecurityUtils.getCurrentUserId())
                .details(Map.of("commentId", commentId))
                .build());

        // Trả response
        return DeleteOrderCommentResponse.builder()
                .commentId(comment.getCommentId())
                .deleted(true)
                .deletedBy(comment.getDeletedBy())
                .deletedAt(comment.getDeletedAt())
                .deleteReason(comment.getDeleteReason())
                .build();
    }

    /**
     * Helper method để lấy OrderID cho việc ghi event log.
     * Nếu comment gắn vào ORDER, trả về ID đó.
     * Nếu comment gắn vào RESULT, tìm TestResult để lấy OrderID của nó.
     *
     * @param comment Comment
     * @return OrderID liên quan, hoặc null nếu không tìm thấy.
     */
    private String getOrderIdForLog(OrderComment comment) {
        if (comment.getTargetType() == CommentTargetType.ORDER) {
            return comment.getTargetId();
        } else if (comment.getTargetType() == CommentTargetType.RESULT) {
            // Giả định TestResultRepository là JpaRepository và TestResult có getOrderId()
            TestResult result = testResultRepository.findById(comment.getTargetId())
                    .orElse(null); // Không nên throw exception ở đây, có thể trả null

            if (result != null) {
                // Giả định TestResult có phương thức getOrderId()
                return result.getOrderId();
            }
        }
        return null; // Trả về null nếu không xác định được
    }
}