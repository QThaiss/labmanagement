/*
 * @ {#} TestService.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.dtos.request.*;
import fit.test_order_service.dtos.response.*;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewMode;
import fit.test_order_service.enums.ReviewStatus;

import java.time.LocalDate;

/*
 * @description: Service interface for Test entity operations
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
public interface TestOrderService {
    TestOrderResponse createTestOrder(CreateTestOrderRequest createTestOrder);

    TestOrderDetailResponse getTestOrderById(String id);

    TestOrderResponse getTestOrderByTestOrderId(String id);

    void deleteTestOrder(String id);

    TestOrderResponse updateTestOrderByCode(String orderCode, UpdateTestOrderRequest request);

    PageResponse<TestOrderResponse> getAllTestOrders(int page, int size, String[] sort, String search,
                                                     LocalDate startDate, LocalDate endDate, OrderStatus status, ReviewStatus reviewStatus,
                                                     ReviewMode reviewMode, Gender gender, String createdBy, String reviewedBy);

    /**
     * Khởi tạo một job chạy ngầm để in kết quả Test Order ra file PDF.
     *
     * @param orderId ID của TestOrder cần in.
     * @param request DTO tùy chọn chứa tên file tùy chỉnh.
     * @return PrintJobResponse chứa thông tin về job vừa được đưa vào hàng đợi.
     */
    PrintJobResponse requestPrintOrder(String orderId, PrintTestOrderRequest request);

    /**
     * Khởi tạo một job chạy ngầm để xuất danh sách Test Order ra file Excel.
     *
     * @param request DTO chứa các tùy chọn export (orderIds, fileName, savePath).
     * @return PrintJobResponse chứa thông tin về job vừa được đưa vào hàng đợi.
     */
    PrintJobResponse requestExportExcel(ExportExcelRequest request);

    /**
     * Thực hiện review một Test Order đã hoàn thành.
     * Cập nhật ReviewStatus và ghi lại các điều chỉnh (nếu có).
     *
     * @param orderId ID của TestOrder.
     * @param request Dữ liệu review, bao gồm các điều chỉnh.
     * @return Thông tin tóm tắt về kết quả review.
     */
    ReviewTestOrderResponse reviewTestOrder(String orderId, ReviewTestOrderHl7Request request);

    /**
     * Tạo đơn xét nghiệm dạng shell từ mã vạch.
     *
     * @param barcode Mã vạch của đơn xét nghiệm.
     * @return Thông tin đơn xét nghiệm được tạo.
     */
    TestOrderResponse createShellOrderFromBarcode(String barcode);


}