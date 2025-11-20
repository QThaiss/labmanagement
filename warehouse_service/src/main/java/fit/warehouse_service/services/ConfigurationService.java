/*
 * @ (#) ConfigurationService.java    1.0    03/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 03/11/2025
 * @version: 1.0
 */

import fit.warehouse_service.dtos.request.CreateConfigurationRequest;
import fit.warehouse_service.dtos.request.ModifyConfigurationRequest;
import fit.warehouse_service.dtos.response.ConfigurationResponse;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.enums.DataType;
import fit.warehouse_service.exceptions.ResourceNotFoundException;

import java.time.LocalDate;

/**
 * Service interface for managing Configuration settings.
 */
public interface ConfigurationService {

    /**
     * Tạo một cấu hình mới.
     *
     * @param request DTO chứa thông tin cấu hình mới.
     * @return DTO của cấu hình đã được tạo.
     * @throws fit.warehouse_service.exceptions.DuplicateResourceException nếu tên cấu hình đã tồn tại.
     * @throws fit.warehouse_service.exceptions.NotFoundException          nếu Group ID không tồn tại.
     */
    ConfigurationResponse createConfiguration(CreateConfigurationRequest request);

    /**
     * Chỉnh sửa một cấu hình hiện có.
     *
     * @param configurationId ID của cấu hình cần chỉnh sửa.
     * @param request         DTO chứa thông tin cập nhật.
     * @return DTO của cấu hình đã được cập nhật.
     */
    ConfigurationResponse modifyConfiguration(String configurationId, ModifyConfigurationRequest request);

    /**
     * Lấy tất cả cấu hình với phân trang, sắp xếp và lọc.
     *
     * @param page      Số trang (bắt đầu từ 0).
     * @param size      Kích thước trang.
     * @param sort      Mảng sắp xếp (ví dụ: ["name,asc", "createdAt,desc"]).
     * @param search    Từ khóa tìm kiếm trong tên hoặc mô tả.
     * @param dataType  Lọc theo loại dữ liệu (nếu có).
     * @param startDate Lọc theo ngày tạo bắt đầu (nếu có).
     * @param endDate   Lọc theo ngày tạo kết thúc (nếu có).
     * @return Trang kết quả chứa danh sách cấu hình.
     */
    PageResponse<ConfigurationResponse> getAllConfigurations(int page, int size, String[] sort, String search, DataType dataType, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy chi tiết một cấu hình theo ID.
     *
     * @param configurationId ID của cấu hình.
     * @return DTO của cấu hình.
     */
    ConfigurationResponse getConfigurationById(String configurationId);

    /**
     * Xóa một cấu hình hệ thống dựa trên ID.
     *
     * @param id ID của cấu hình cần xóa.
     * @throws ResourceNotFoundException nếu không tìm thấy cấu hình.
     */
    void deleteConfiguration(String id);
}
