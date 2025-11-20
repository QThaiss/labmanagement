package fit.test_order_service.services;

import fit.test_order_service.dtos.request.AddCommentRequest;
import fit.test_order_service.dtos.request.DeleteOrderCommentRequest;
import fit.test_order_service.dtos.request.ReplyCommentRequest;
import fit.test_order_service.dtos.request.UpdateOrderCommentRequest;
import fit.test_order_service.dtos.response.AddCommentResponse;
import fit.test_order_service.dtos.response.DeleteOrderCommentResponse;
import fit.test_order_service.dtos.response.OrderCommentResponse;
import fit.test_order_service.dtos.response.UpdateOrderCommentResponse;
import fit.test_order_service.entities.OrderComment;
import jakarta.transaction.Transactional;

import java.util.Optional;

public interface OrderCommentService {
    AddCommentResponse addComment(AddCommentRequest request);

    // --- THÊM PHƯƠNG THỨC NÀY ---
    /**
     * Trả lời một comment đã có
     * @param parentCommentId ID của comment cha
     * @param request Nội dung reply
     * @return Thông tin comment vừa tạo (reply)
     */
    OrderCommentResponse replyToComment(String parentCommentId, ReplyCommentRequest request);

    UpdateOrderCommentResponse modifyComment(String commentId,UpdateOrderCommentRequest request);
    DeleteOrderCommentResponse deleteComment(String commentId, DeleteOrderCommentRequest request);

}
