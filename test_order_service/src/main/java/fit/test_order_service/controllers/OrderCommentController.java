package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.AddCommentRequest;
import fit.test_order_service.dtos.request.DeleteOrderCommentRequest;
import fit.test_order_service.dtos.request.ReplyCommentRequest;
import fit.test_order_service.dtos.request.UpdateOrderCommentRequest;
import fit.test_order_service.dtos.response.*;
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
    public ResponseEntity<ApiResponse<AddCommentResponse>> createOrderComment(
            @Valid @RequestBody AddCommentRequest request
    ) {
        AddCommentResponse response = orderCommentService.addComment(request);

        ApiResponse<AddCommentResponse> apiResponse = ApiResponse.<AddCommentResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Comment created successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<UpdateOrderCommentResponse>> modifyOrderComment(
            @PathVariable String commentId,
            @Valid @RequestBody UpdateOrderCommentRequest request
    ) {
        UpdateOrderCommentResponse response = orderCommentService.modifyComment(commentId, request);

        ApiResponse<UpdateOrderCommentResponse> apiResponse = ApiResponse.<UpdateOrderCommentResponse>builder()
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
