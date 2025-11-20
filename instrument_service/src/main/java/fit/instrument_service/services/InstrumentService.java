/*
 * @ {#} InstrumentService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

import fit.instrument_service.dtos.request.ChangeInstrumentModeRequest;
import fit.instrument_service.dtos.request.InstallReagentRequest;
import fit.instrument_service.dtos.request.ModifyReagentStatusRequest;
import fit.instrument_service.dtos.response.InstrumentReagentResponse;
import fit.instrument_service.dtos.response.InstrumentResponse;
import fit.instrument_service.events.ConfigurationCreatedEvent;
import fit.instrument_service.events.ConfigurationDeletedEvent;
import fit.instrument_service.events.InstrumentActivatedEvent;
import fit.instrument_service.events.InstrumentDeactivatedEvent;

/*
 * @description: Service interface for managing Instruments.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface InstrumentService {
    /*
     * Thay đổi chế độ hoạt động của thiết bị
     *
     * @param instrumentId ID của thiết bị cần thay đổi chế độ
     * @param request      Yêu cầu chứa thông tin chế độ mới và lý do thay đổi
     * @return Thông tin thiết bị sau khi thay đổi chế độ
     */
    InstrumentResponse changeInstrumentMode(String instrumentId, ChangeInstrumentModeRequest request);

    /*
     * Tạo thiết bị mới từ sự kiện kích hoạt thiết bị
     *
     * @param event Sự kiện kích hoạt thiết bị
     */
    void handleInstrumentActivation(InstrumentActivatedEvent event);

    /*
     * Cập nhật trạng thái thiết bị từ sự kiện vô hiệu hóa thiết bị
     *
     * @param event Sự kiện vô hiệu hóa thiết bị
     */
    void handleInstrumentDeactivated(InstrumentDeactivatedEvent event);

    InstrumentReagentResponse installReagent(String instrumentId, InstallReagentRequest request);

    InstrumentReagentResponse modifyReagentStatus(String instrumentId, String reagentId, ModifyReagentStatusRequest request);

    /**
     * Xử lý logic nghiệp vụ khi nhận được sự kiện tạo cấu hình.
     *
     * @param event Sự kiện chứa thông tin của cấu hình cần tạo
     */
    void handleConfigurationCreation(ConfigurationCreatedEvent event);

    /**
     * Xử lý logic nghiệp vụ khi nhận được sự kiện xóa cấu hình.
     *
     * @param event Sự kiện chứa ID của cấu hình cần xóa
     */
    void handleConfigurationDeletion(ConfigurationDeletedEvent event);


}
