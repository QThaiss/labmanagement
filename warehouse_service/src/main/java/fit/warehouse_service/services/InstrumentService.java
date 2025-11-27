/*
<<<<<<< HEAD
 * @ {#} InstrumentStatusService.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.ActivateInstrumentRequest;
import fit.warehouse_service.dtos.request.CheckInstrumentStatusRequest;
import fit.warehouse_service.dtos.request.CreateInstrumentRequest;
import fit.warehouse_service.dtos.request.DeactivateInstrumentRequest;
import fit.warehouse_service.dtos.response.InstrumentActivationResponse;
import fit.warehouse_service.dtos.response.InstrumentResponse;
import fit.warehouse_service.dtos.response.InstrumentStatusResponse;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.entities.Instrument;

import java.time.LocalDate;
import java.util.List;

/*
 * @description: Service interface for managing instrument status operations.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
public interface InstrumentService {
    /**
     * Kiểm tra trạng thái của một Instrument dựa trên yêu cầu.
     *
     * @param request DTO chứa thông tin kiểm tra trạng thái.
     * @return DTO phản hồi trạng thái của Instrument.
     */
    InstrumentStatusResponse checkInstrumentStatus(CheckInstrumentStatusRequest request);

    /**
     * Tạo một Instrument mới dựa trên yêu cầu.
     *
     * @param request DTO chứa thông tin tạo mới.
     * @return Entity Instrument đã được lưu.
     */
    Instrument createInstrument(CreateInstrumentRequest request);

    /**
     * Kích hoạt một Instrument dựa trên yêu cầu.
     *
     * @param request DTO chứa thông tin kích hoạt.
     * @return DTO phản hồi sau khi kích hoạt.
     */
    InstrumentActivationResponse activateInstrument(ActivateInstrumentRequest request);

    /**
     * Vô hiệu hóa một Instrument dựa trên yêu cầu.
     *
     * @param request DTO chứa thông tin vô hiệu hóa.
     * @return DTO phản hồi sau khi vô hiệu hóa.
     */
    InstrumentActivationResponse deactivateInstrument(DeactivateInstrumentRequest request);

    /**
     * Lấy danh sách tất cả các instrument.
     * Dữ liệu được sắp xếp theo ngày cập nhật mới nhất (hoặc ngày tạo nếu chưa cập nhật).
     *
     * @return Danh sách các Instrument.
     */
    List<Instrument> getAllInstruments();

    /**
     * Lấy danh sách tất cả các instrument (đã cập nhật để phân trang và lọc).
     *
     * @param page      Số trang (bắt đầu từ 0).
     * @param size      Kích thước trang.
     * @param sort      Mảng sắp xếp (ví dụ: ["name,asc", "createdAt,desc"]).
     * @param search    Từ khóa tìm kiếm trong tên hoặc IP.
     * @param startDate Lọc theo ngày tạo bắt đầu (nếu có).
     * @param endDate   Lọc theo ngày tạo kết thúc (nếu có).
     * @return Trang kết quả chứa danh sách InstrumentResponse.
     */
    PageResponse<InstrumentResponse> getAllInstruments(int page, int size, String[] sort, String search, String configType, LocalDate startDate, LocalDate endDate);
}
