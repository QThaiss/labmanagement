package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.AddCommentRequest;
import fit.test_order_service.dtos.request.DeleteOrderCommentRequest;
import fit.test_order_service.dtos.request.ReplyCommentRequest;
import fit.test_order_service.dtos.request.UpdateOrderCommentRequest;
// Chỉ giữ lại OrderCommentResponse và các response cần thiết khác
import fit.test_order_service.dtos.response.DeleteOrderCommentResponse;
import fit.test_order_service.dtos.response.OrderCommentResponse;
import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.services.OrderCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/order-comments")
@RequiredArgsConstructor
public class OrderCommentController {

    private final OrderCommentService orderCommentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<OrderCommentResponse>> createOrderComment(
            @Valid @RequestBody AddCommentRequest request
    ) {
        OrderCommentResponse response = orderCommentService.addComment(request);

        // Thay đổi kiểu generic từ AddCommentResponse sang OrderCommentResponse
        ApiResponse<OrderCommentResponse> apiResponse = ApiResponse.<OrderCommentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Comment created successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<OrderCommentResponse>> modifyOrderComment(
            @PathVariable String commentId,
            @Valid @RequestBody UpdateOrderCommentRequest request
    ) {
        OrderCommentResponse response = orderCommentService.modifyComment(commentId, request);

        // Thay đổi kiểu generic từ UpdateOrderCommentResponse sang OrderCommentResponse
        ApiResponse<OrderCommentResponse> apiResponse = ApiResponse.<OrderCommentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Comment updated successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/{commentId}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<DeleteOrderCommentResponse>> deleteOrderComment(
            @PathVariable String commentId,
            @Valid @RequestBody DeleteOrderCommentRequest request
    ) {
        DeleteOrderCommentResponse response = orderCommentService.deleteComment(commentId, request);

        ApiResponse<DeleteOrderCommentResponse> apiResponse = ApiResponse.<DeleteOrderCommentResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Comment deleted successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/{parentCommentId}/reply")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<OrderCommentResponse>> createReplyComment(
            @PathVariable String parentCommentId,
            @Valid @RequestBody ReplyCommentRequest request
    ) {
        OrderCommentResponse response = orderCommentService.replyToComment(parentCommentId, request);

        ApiResponse<OrderCommentResponse> apiResponse = ApiResponse.<OrderCommentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Reply created successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}