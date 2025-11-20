/*
 * @ {#} InternalTestOrderController.java   1.0     13/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.controllers;

import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.TestOrderDetailResponse;
import fit.test_order_service.dtos.response.TestOrderResponse;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.services.TestOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   13/11/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/internal/test-orders")
@RequiredArgsConstructor
public class InternalTestOrderController {

    private final TestOrderService testOrderService;

    /**
     * Endpoint này khớp với getTestOrderById của FeignClient.
     * Dùng để instrument_service kiểm tra xem TestOrder có tồn tại không.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestOrderResponse>> getTestOrderById(@PathVariable("id") String id) {
        TestOrderResponse order = testOrderService.getTestOrderByTestOrderId(id);

        return ResponseEntity.ok(ApiResponse.success(order, "Test order retrieved successfully"));
    }

    /**
     * Endpoint này khớp với createTestOrder của FeignClient.
     * Dùng để instrument_service tự động tạo một TestOrder "vỏ" khi
     * nhận được mẫu mà không có testOrderId.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTestOrder(@RequestBody Map<String, Object> testOrderData) {

        String barcode = (String) testOrderData.get("barcode");
        if (barcode == null || barcode.isBlank()) {
            throw new BadRequestException("Barcode is required for internal order creation");
        }

        // Gọi một phương thức service mới (bạn sẽ cần thêm ở bước 2)
        TestOrderResponse newOrder = testOrderService.createShellOrderFromBarcode(barcode);

        // Trả về chính xác Map chứa "orderId" mà instrument_service mong đợi
        Map<String, Object> data = Map.of("orderId", newOrder.getId());

        return new ResponseEntity<>(
                ApiResponse.success(data, "Test order auto-created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/full/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderDetailResponse>> getTestOrderDetailsById(@PathVariable String id) {
        TestOrderDetailResponse response = testOrderService.getTestOrderById(id);
        ApiResponse<TestOrderDetailResponse> apiResponse = ApiResponse.<TestOrderDetailResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Test order details retrieved successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
