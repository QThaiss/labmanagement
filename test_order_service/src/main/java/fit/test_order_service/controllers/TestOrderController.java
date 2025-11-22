/*
 * @ {#} TestController.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.controllers;

import fit.test_order_service.dtos.request.*;
import fit.test_order_service.dtos.response.*;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewMode;
import fit.test_order_service.enums.ReviewStatus;
import fit.test_order_service.services.TestOrderService;
import fit.test_order_service.validators.RequestParamValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/*
 * @description: Controller for Test entity operations
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/test-orders")
@RequiredArgsConstructor
public class TestOrderController {

    private final TestOrderService testOrderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> createTestOrder(@Valid @RequestBody CreateTestOrderRequest request) {
        TestOrderResponse response = testOrderService.createTestOrder(request);
        ApiResponse<TestOrderResponse> apiResponse = ApiResponse.<TestOrderResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Patient's test order created successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderDetailResponse>> getTestOrderById(@PathVariable String id) {
        TestOrderDetailResponse response = testOrderService.getTestOrderById(id);
        ApiResponse<TestOrderDetailResponse> apiResponse = ApiResponse.<TestOrderDetailResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Test order details retrieved successfully")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/{testOrderById}/full")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> getTestOrderByTestOrderId(@PathVariable String testOrderById) {
        TestOrderResponse response = testOrderService.getTestOrderByTestOrderId(testOrderById);

        return ResponseEntity.ok(ApiResponse.success(response, "Test order retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTestOrder(@PathVariable String id) {
        testOrderService.deleteTestOrder(id);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Patient's test order deleted successfully.")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/{orderCode}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<TestOrderResponse>> updateTestOrderByCode(
            @PathVariable String orderCode,
            @Valid @RequestBody UpdateTestOrderRequest request) {

        TestOrderResponse response = testOrderService.updateTestOrderByCode(orderCode, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Test order updated successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<TestOrderResponse>>> getAllTestOrders(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be less than zero")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must not be less than one")
            int size,

            @RequestParam(name = "sort", required = false)
            String[] sort,

            @RequestParam(name = "search", required = false)
            String search,

            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(name = "status", required = false)
            OrderStatus status,

            @RequestParam(name = "reviewStatus", required = false)
            ReviewStatus reviewStatus,

            @RequestParam(name = "reviewMode", required = false)
            ReviewMode reviewMode,

            @RequestParam(name = "gender", required = false)
            Gender gender,

            @RequestParam(name = "createdBy", required = false)
            String createdBy,

            @RequestParam(name = "reviewedBy", required = false)
            String reviewedBy
    ) {

        PageResponse<TestOrderResponse> response = testOrderService.getAllTestOrders(
                page, size, sort, search, startDate, endDate, status, reviewStatus, reviewMode, gender, createdBy, reviewedBy);

        return ResponseEntity.ok(ApiResponse.success(response, "Test orders retrieved successfully"));
    }

    /**
     * Endpoint để yêu cầu in kết quả của một Test Order.
     * Quá trình in sẽ chạy ngầm.
     *
     * @param orderId ID của TestOrder cần in.
     * @param request DTO tùy chọn chứa tên file mong muốn.
     * @return Thông tin về job in vừa được đưa vào hàng đợi.
     */
    @PostMapping("/{orderId}/print")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<ApiResponse<PrintJobResponse>> requestPrintTestOrder(
            @PathVariable String orderId,
            @Valid @RequestBody(required = false) PrintTestOrderRequest request) {

        PrintJobResponse jobResponse = testOrderService.requestPrintOrder(orderId, request);

        ApiResponse<PrintJobResponse> apiResponse = ApiResponse.<PrintJobResponse>builder()
                .success(true)
                .status(HttpStatus.ACCEPTED.value()) // 202 Accepted: Yêu cầu đã được chấp nhận, đang xử lý
                .message("Print job for test order " + orderId + " has been queued.")
                .data(jobResponse)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Endpoint để yêu cầu xuất danh sách Test Order ra file Excel.
     * Quá trình export sẽ chạy ngầm.
     *
     * @param request DTO chứa danh sách orderIds (tùy chọn), tên file và đường dẫn lưu tùy chỉnh.
     * @return Thông tin về job export vừa được đưa vào hàng đợi.
     */
    @PostMapping("/export-excel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") // Chỉ Admin/Manager được export list? (Điều chỉnh nếu cần)
    public ResponseEntity<ApiResponse<PrintJobResponse>> requestExportTestOrdersExcel(
            @Valid @RequestBody ExportExcelRequest request) { // Request body là bắt buộc

        PrintJobResponse jobResponse = testOrderService.requestExportExcel(request);

        ApiResponse<PrintJobResponse> apiResponse = ApiResponse.<PrintJobResponse>builder()
                .success(true)
                .status(HttpStatus.ACCEPTED.value()) // 202 Accepted
                .message("Export Excel job has been queued.")
                .data(jobResponse)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Endpoint để review một Test Order đã COMPLETED.
     * Cập nhật ReviewStatus thành REVIEWED và ghi lại bất kỳ điều chỉnh kết quả nào (nếu có) thông qua HL7.
     *
     * @param orderId ID của TestOrder cần review.
     * @param request DTO chứa chế độ review, ghi chú, và một tin nhắn HL7 (tùy chọn) để điều chỉnh.
     * @return Thông tin về kết quả review.
     */
    @PostMapping("/{orderId}/review")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<ReviewTestOrderResponse>> reviewTestOrder(
            @PathVariable String orderId,
            @Valid @RequestBody ReviewTestOrderHl7Request request) {

        ReviewTestOrderResponse response = testOrderService.reviewTestOrder(orderId, request);

        ApiResponse<ReviewTestOrderResponse> apiResponse = ApiResponse.<ReviewTestOrderResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Test order reviewed successfully.")
                .data(response)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // Api giao tiếp nội bộ từ Instrument Service để tự động tạo Test Order từ mẫu xét nghiệm
    @PostMapping("/internal/auto-create")
    public ResponseEntity<ApiResponse<TestOrderResponse>> autoCreateFromInstrument(
            @Valid @RequestBody AutoCreateTestOrderRequest request
    ) {
        TestOrderResponse response = testOrderService.autoCreateTestOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Auto-created test order from instrument sample"));
    }
}