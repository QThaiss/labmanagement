/*
 * @ {#} TestParameterService.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.CreateTestParameterRequest;
import fit.warehouse_service.dtos.request.UpdateTestParameterRequest;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.TestParameterResponse;

import java.time.LocalDate;
import java.util.List;

/*
 * @description: Service interface for managing ParameterRange entities
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
public interface TestParameterService {
    /**
     * Tạo mới một TestParameter
     *
     * @param request dữ liệu yêu cầu tạo TestParameter
     *
     * @return TestParameterResponse chứa thông tin của TestParameter vừa tạo
     */
    TestParameterResponse createTestParameter(CreateTestParameterRequest request);

    /**
     * Lấy thông tin một TestParameter theo abbreviation
     *
     * @param abbreviation viết tắt của TestParameter cần lấy
     *
     * @return TestParameterResponse chứa thông tin của TestParameter
     */
    TestParameterResponse getTestParameterByAbbreviation(String abbreviation);

    /**
     * Cập nhật một TestParameter hiện có
     *
     * @param testParameterId id của TestParameter cần cập nhật
     * @param request dữ liệu yêu cầu cập nhật TestParameter
     *
     * @return TestParameterResponse chứa thông tin của TestParameter vừa cập nhật
     */
    TestParameterResponse updateTestParameter(String testParameterId, UpdateTestParameterRequest request);

    /**
     * Xóa mềm một TestParameter
     *
     * @param testParameterId id của TestParameter cần xóa
     */
    void deleteTestParameter(String testParameterId);

    /**
     * Khôi phục một TestParameter đã bị xóa mềm
     *
     * @param testParameterId id của TestParameter cần khôi phục
     *
     * @return TestParameterResponse chứa thông tin của TestParameter vừa khôi phục
     */
    TestParameterResponse restoreTestParameter(String testParameterId);

    /**
     * Lấy danh sách phân trang của TestParameter với các tùy chọn lọc và sắp xếp
     *
     * @param page số trang hiện tại
     * @param size kích thước trang
     * @param sort mảng các trường để sắp xếp
     * @param search từ khóa tìm kiếm
     * @param startDate ngày bắt đầu để lọc
     * @param endDate ngày kết thúc để lọc
     *
     * @return PageResponse chứa danh sách TestParameterResponse theo trang
     */
    PageResponse<TestParameterResponse> getAllTestParameters(int page, int size, String[] sort, String search, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy thông tin một TestParameter theo testParameterId
     *
     * @param testParameterId id của TestParameter cần lấy
     *
     * @return TestParameterResponse chứa thông tin của TestParameter
     */
    TestParameterResponse getTestParameterByTestParameterId(String testParameterId);

    boolean validateTestParametersExist(List<String> ids);
}
