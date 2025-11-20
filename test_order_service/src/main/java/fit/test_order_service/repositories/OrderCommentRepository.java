package fit.test_order_service.repositories;

import fit.test_order_service.entities.OrderComment;
import fit.test_order_service.enums.CommentTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderCommentRepository extends JpaRepository<OrderComment, String> {

//    int countByAuthorUserIdAndOrderIdAndCreatedAtAfter(String authorUserId, String orderId, LocalDateTime since);
//
//    boolean existsByAuthorUserIdAndOrderIdAndContentAndCreatedAtAfter(
//            String authorUserId, String orderId, String content, LocalDateTime since);
//    Optional<OrderComment> findByCommentIdAndDeletedAtIsNull(String commentId);
//
//    int countByAuthorUserIdAndResultIdAndCreatedAtAfter(String authorUserId, String resultId, LocalDateTime since);
//
//    boolean existsByAuthorUserIdAndResultIdAndContentAndCreatedAtAfter(
//            String authorUserId, String resultId, String content, LocalDateTime since);


    int countByAuthorUserIdAndTargetIdAndCreatedAtAfter(String authorUserId, String targetId, LocalDateTime since);

    boolean existsByAuthorUserIdAndTargetIdAndContentAndCreatedAtAfter(
            String authorUserId, String targetId, String content, LocalDateTime since);

    Optional<OrderComment> findByCommentIdAndDeletedAtIsNull(String commentId);

    /**
     * Tìm các comment cấp cao nhất (parentId = null) cho một targetId cụ thể.
     */
    List<OrderComment> findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(CommentTargetType targetType, String targetId);

    /**
     * Tìm các comment cấp cao nhất (parentId = null) cho một danh sách các targetId.
     */
    List<OrderComment> findByTargetTypeAndTargetIdInAndParentIdIsNullOrderByCreatedAtAsc(CommentTargetType targetType, List<String> targetIds);
}
