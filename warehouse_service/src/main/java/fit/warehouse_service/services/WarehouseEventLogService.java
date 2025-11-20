/*
 * @ (#) WarehouseEventLogService.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.entities.ConfigurationSetting;
import fit.warehouse_service.entities.Instrument;
import fit.warehouse_service.entities.ReagentSupplyHistory;
import fit.warehouse_service.entities.ReagentUsageHistory;
import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.enums.WarehouseActionType;

import java.time.LocalDateTime;

/**
 * Interface cho dịch vụ ghi log các sự kiện trong kho.
 */
public interface WarehouseEventLogService {

    /**
     * Ghi lại một sự kiện hành động.
     *
     * @param action     Loại hành động (vi phạm, v.v.).
     * @param entityId   ID của thực thể (ví dụ: ID của Instrument).
     * @param entityType Tên loại thực thể (ví dụ: "Instrument").
     * @param details    Chi tiết sự kiện (thường là JSON).
     */
    void logEvent(WarehouseActionType action, String entityId, String entityType, String details);

    /**
     * Tạo một chuỗi JSON chi tiết cho sự kiện tạo mới instrument.
     *
     * @param instrument Instrument vừa được tạo.
     * @return Chuỗi JSON mô tả chi tiết.
     */
    String createInstrumentCreatedDetails(Instrument instrument);

    /**
     * Tạo một chuỗi JSON chi tiết cho sự kiện kích hoạt hoặc vô hiệu hóa instrument.
     *
     * @param instrument          Instrument được kích hoạt hoặc vô hiệu hóa.
     * @param action              Hành động thực hiện ("activate" hoặc "deactivate").
     * @param reason              Lý do cho hành động.
     * @param previousActiveState Trạng thái hoạt động trước đó.
     * @param previousStatus      Trạng thái trước đó của instrument.
     * @return Chuỗi JSON mô tả chi tiết.
     */
    String createInstrumentActivationDetails(Instrument instrument, String action, String reason,
                                             boolean previousActiveState, InstrumentStatus previousStatus);

    /**
     * Tạo một chuỗi JSON chi tiết cho sự kiện lên lịch xóa instrument.
     *
     * @param instrument    Instrument được lên lịch xóa.
     * @param scheduledTime Thời gian dự kiến xóa.
     * @param reason        Lý do xóa.
     * @return Chuỗi JSON mô tả chi tiết.
     */
    String createScheduledDeletionDetails(Instrument instrument, LocalDateTime scheduledTime, String reason);

    /**
     * Tạo một chuỗi JSON chi tiết cho sự kiện hủy bỏ lịch xóa instrument.
     *
     * @param instrumentId          ID của instrument.
     * @param originalScheduledTime Thời gian xóa dự kiến ban đầu.
     * @return Chuỗi JSON mô tả chi tiết.
     */
    String createDeletionCancellationDetails(String instrumentId, LocalDateTime originalScheduledTime);

    /**
     * Tạo một chuỗi JSON chi tiết cho sự kiện xóa instrument vĩnh viễn.
     *
     * @param instrument Instrument bị xóa.
     * @return Chuỗi JSON mô tả chi tiết.
     */
    String createInstrumentDeletionDetails(Instrument instrument);

    String createReagentReceivedDetails(ReagentSupplyHistory history);

    String createReagentUsageDetails(ReagentUsageHistory history);

    String createConfigurationCreatedDetails(ConfigurationSetting config);

    String createConfigurationModifiedDetails(ConfigurationSetting config, String oldValue, String newValue, String reason);
}