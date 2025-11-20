/*
 * @ {#} InstrumentServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import feign.FeignException;
import fit.instrument_service.client.WarehouseFeignClient;
import fit.instrument_service.client.dtos.ReagentLotStatusResponse;
import fit.instrument_service.dtos.request.ChangeInstrumentModeRequest;
import fit.instrument_service.dtos.request.InstallReagentRequest;
import fit.instrument_service.dtos.request.ModifyReagentStatusRequest;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.InstrumentReagentResponse;
import fit.instrument_service.dtos.response.InstrumentResponse;
import fit.instrument_service.dtos.response.VendorResponse;
import fit.instrument_service.embedded.Vendor;
import fit.instrument_service.entities.Configuration;
import fit.instrument_service.entities.Instrument;
import fit.instrument_service.entities.InstrumentModeLog;
import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.AuditAction;
import fit.instrument_service.enums.InstrumentMode;
import fit.instrument_service.enums.InstrumentStatus;
import fit.instrument_service.enums.ReagentStatus;
import fit.instrument_service.events.ConfigurationCreatedEvent;
import fit.instrument_service.events.ConfigurationDeletedEvent;
import fit.instrument_service.events.InstrumentActivatedEvent;
import fit.instrument_service.events.InstrumentDeactivatedEvent;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.mappers.InstrumentMapper;
import fit.instrument_service.repositories.ConfigurationRepository;
import fit.instrument_service.repositories.InstrumentModeLogRepository;
import fit.instrument_service.repositories.InstrumentReagentRepository;
import fit.instrument_service.repositories.InstrumentRepository;
import fit.instrument_service.services.AuditLogService;
import fit.instrument_service.services.InstrumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * @description: Implementation of InstrumentService for managing Instruments.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {
    private final InstrumentRepository instrumentRepository;

    private final InstrumentModeLogRepository instrumentModeLogRepository;

    private final InstrumentMapper instrumentMapper;

    private final InstrumentReagentRepository instrumentReagentRepository;

    private final AuditLogService auditLogService;

    private final WarehouseFeignClient warehouseFeignClient;

    private final ConfigurationRepository configurationRepository;

    @Override
    @Transactional
    public InstrumentResponse changeInstrumentMode(String instrumentId, ChangeInstrumentModeRequest request) {
        // Tìm Instrument theo ID
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with id: " + instrumentId));

        // Lấy thông tin từ request
        InstrumentMode newMode = InstrumentMode.valueOf(request.getNewMode());
        String reason = request.getReason();
        InstrumentMode previousMode = instrument.getMode();

        // Phải cung cấp lý do khi chuyển sang Maintenance hoặc Inactive
        if ((newMode == InstrumentMode.MAINTENANCE || newMode == InstrumentMode.INACTIVE) && !StringUtils.hasText(reason)) {
            throw new IllegalArgumentException("Reason is required when changing mode to " + newMode);
        }

        // Ghi log
        InstrumentModeLog logEntry = new InstrumentModeLog();
        logEntry.setInstrumentId(instrument.getId());
        logEntry.setPreviousMode(previousMode);
        logEntry.setNewMode(newMode);
        logEntry.setReason(reason);

        instrumentModeLogRepository.save(logEntry);

        // Cập nhật Instrument
        instrument.setMode(newMode);
        instrument.setLastModeChangeReason(reason);
        Instrument updatedInstrument = instrumentRepository.save(instrument);

        // Trả về Response
        return instrumentMapper.toResponse(updatedInstrument);
    }

    @Override
    @Transactional
    public void handleInstrumentActivation(InstrumentActivatedEvent event) {
        // Tìm xem instrument có tồn tại không
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(event.getId());

        Instrument instrument;

        if (instrumentOpt.isPresent()) {
            log.info("Handling Re-Activation event for existing instrument id: {}", event.getId());
            instrument = instrumentOpt.get();

            // Cập nhật lại thông tin
            instrument.setName(event.getName());
            instrument.setModel(event.getModel());
            instrument.setType(event.getType());
            instrument.setSerialNumber(event.getSerialNumber());
            instrument.setVendor(new Vendor(
                    event.getVendorId(),
                    event.getVendorName(),
                    event.getVendorContact()
            ));

            // Reset trạng thái từ (INACTIVE + ERROR) về (INACTIVE + AVAILABLE)
            instrument.setMode(InstrumentMode.INACTIVE);
            instrument.setStatus(InstrumentStatus.AVAILABLE);

            // Ghi log cho việc "Re-activate"
            InstrumentModeLog logEntry = new InstrumentModeLog();
            logEntry.setInstrumentId(instrument.getId());
            logEntry.setPreviousMode(InstrumentMode.INACTIVE);
            logEntry.setNewMode(InstrumentMode.INACTIVE);
            logEntry.setReason("Re-activated from Warehouse.");
            instrumentModeLogRepository.save(logEntry);
        } else {
            log.info("Handling new InstrumentActivatedEvent for id: {}", event.getId());
            instrument = new Instrument();
            instrument.setId(event.getId()); // id từ event

            // Map thông tin
            instrument.setName(event.getName());
            instrument.setModel(event.getModel());
            instrument.setType(event.getType());
            instrument.setSerialNumber(event.getSerialNumber());
            instrument.setVendor(new Vendor(
                    event.getVendorId(),
                    event.getVendorName(),
                    event.getVendorContact()
            ));

            // Set trạng thái ban đầu
            instrument.setMode(InstrumentMode.INACTIVE);
            instrument.setStatus(InstrumentStatus.AVAILABLE);
        }

        // Lưu
        instrumentRepository.save(instrument);
        log.info("Successfully upserted instrument record with id: {}", instrument.getId());
    }

    @Override
    public void handleInstrumentDeactivated(InstrumentDeactivatedEvent event) {
        String instrumentId = event.getId();

        // Tìm instrument
        Optional<Instrument> instrumentOpt = instrumentRepository.findById(instrumentId);

        // Nếu không tìm thấy, ghi log và ném ngoại lệ
        if (instrumentOpt.isEmpty()) {
            log.warn("Received InstrumentDeactivatedEvent for unknown instrument id: {}. Skipping.", instrumentId);
            throw new NotFoundException("Instrument not found with id: " + instrumentId);
        }

        Instrument instrument = instrumentOpt.get();
        log.info("Handling InstrumentDeactivatedEvent for instrument id: {}", instrumentId);

        InstrumentMode previousMode = instrument.getMode();

        // Cập nhật trạng thái
        // Set Mode = INACTIVE và Status = ERROR để vô hiệu hóa hoàn toàn
        instrument.setMode(InstrumentMode.INACTIVE);
        instrument.setStatus(InstrumentStatus.ERROR);
        instrument.setLastModeChangeReason("Deactivated from Warehouse.");

        instrumentRepository.save(instrument);

        // Ghi log kiểm toán
        InstrumentModeLog logEntry = new InstrumentModeLog();
        logEntry.setInstrumentId(instrument.getId());
        logEntry.setPreviousMode(previousMode);
        logEntry.setNewMode(InstrumentMode.INACTIVE);
        logEntry.setReason("Deactivated from Warehouse.");
        instrumentModeLogRepository.save(logEntry);

        log.info("Successfully processed deactivation for instrument id: {}", instrument.getId());
    }

    @Override
    @Transactional
    public InstrumentReagentResponse installReagent(String instrumentId, InstallReagentRequest request) {
        log.info("Installing reagent onto instrument {}: {}", instrumentId, request.getLotNumber());

        // === BƯỚC 1: VALIDATION TỒN TẠI (Vendor + LotNumber) ===
        try {
            log.debug("Validating existence with warehouse for vendor: {} and lot: {}", request.getVendorId(), request.getLotNumber());

            // Cuộc gọi 1: Kiểm tra vendorId và lotNumber có khớp trong lịch sử nhập kho không
            warehouseFeignClient.validateReagentStock(request.getVendorId(), request.getLotNumber());

            log.info("Existence validation successful for vendor: {} and lot: {}", request.getVendorId(), request.getLotNumber());

        } catch (FeignException.NotFound e) {
            log.warn("Validation failed: Vendor ID {} or Lot Number {} not found in warehouse.", request.getVendorId(), request.getLotNumber());
            throw new NotFoundException("Validation failed: Reagent stock not found in warehouse for the given Vendor ID and Lot Number.");
        } catch (Exception e) {
            log.error("Error during existence validation call: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to verify reagent stock (existence): " + e.getMessage());
        }

        // === BƯỚC 2: VALIDATION SỐ LƯỢNG TỒN KHO (LotNumber) ===
        ReagentLotStatusResponse lotStatus;
        try {
            log.debug("Fetching quantity from warehouse for lot: {}", request.getLotNumber());

            // Cuộc gọi 2: Lấy số lượng tồn kho của Lô
            ApiResponse<ReagentLotStatusResponse> apiResponse = warehouseFeignClient.getReagentLotStatus(request.getLotNumber());

            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                throw new RuntimeException("Failed to get reagent lot status from warehouse. Empty response.");
            }

            lotStatus = apiResponse.getData();
            log.info("Quantity validation successful. Lot ID: {}, Current Qty: {}", lotStatus.getReagentLotId(), lotStatus.getCurrentQuantity());

        } catch (FeignException.NotFound e) {
            // Trường hợp này không nên xảy ra nếu Bước 1 thành công, nhưng vẫn bắt lỗi
            log.warn("Validation failed: Lot Number {} not found in ReagentLot table.", request.getLotNumber());
            throw new NotFoundException("Validation failed: Reagent Lot Number not found.");
        } catch (Exception e) {
            log.error("Error during quantity validation call: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to verify reagent stock (quantity): " + e.getMessage());
        }

        VendorResponse vendorDetails;
        try {
            log.debug("Fetching vendor details from warehouse for ID: {}", request.getVendorId());
            ApiResponse<VendorResponse> vendorApiResponse = warehouseFeignClient.getVendorById(request.getVendorId());

            if (vendorApiResponse == null || !vendorApiResponse.isSuccess() || vendorApiResponse.getData() == null) {
                throw new RuntimeException("Failed to get vendor details from warehouse. Empty response.");
            }
            vendorDetails = vendorApiResponse.getData();
            log.info("Successfully fetched vendor details: {}", vendorDetails.getName());

        } catch (FeignException.NotFound e) {
            log.warn("Fetch failed: Vendor ID {} not found in warehouse.", request.getVendorId());
            // Lỗi này không nên xảy ra nếu Bước 1 (validateReagentStock) thành công, nhưng vẫn xử lý
            throw new NotFoundException("Validation failed: Vendor details not found in warehouse.");
        } catch (Exception e) {
            log.error("Error during vendor details fetch: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to fetch vendor details: " + e.getMessage());
        }
        // --- Kiểm tra số lượng (THEO YÊU CẦU CỦA BẠN) ---
        // So sánh số lượng tồn kho (double) với số lượng cài đặt (Integer)
        if (lotStatus.getCurrentQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient stock in warehouse for LotNumber %s. Requested: %d, Available: %.2f",
                    request.getLotNumber(), request.getQuantity(), lotStatus.getCurrentQuantity()
            ));
        }

        // === BƯỚC 3: TIẾP TỤC LOGIC CÀI ĐẶT NHƯ CŨ (KHÔNG TRỪ KHO) ===

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with id: " + instrumentId));

        // ... (Kiểm tra trùng lặp trên máy)
        List<InstrumentReagent> existing = instrumentReagentRepository
                .findByInstrumentIdAndLotNumberAndIsDeletedFalse(instrumentId, request.getLotNumber());
        if (!existing.isEmpty()) {
            // ... (Ném lỗi)
        }

        // 4. Tạo entity InstrumentReagent
        InstrumentReagent reagent = new InstrumentReagent();
        reagent.setInstrumentId(instrumentId);
        reagent.setReagentName(request.getReagentName());
        reagent.setLotNumber(request.getLotNumber());
        reagent.setQuantity(request.getQuantity()); // Số lượng của chai/hộp này
        reagent.setExpirationDate(request.getExpirationDate());
        reagent.setVendor(new Vendor(
                vendorDetails.getId(),
                vendorDetails.getName(),
                vendorDetails.getContactPerson() // Hoặc vendorDetails.getPhone() tùy bạn chọn
        )); // Chỉ lưu ID vendor
        reagent.setStatus(ReagentStatus.NOT_IN_USE);
        reagent.setDeleted(false);

        // 6. Lưu vào DB (của instrument_service)
        InstrumentReagent savedReagent = instrumentReagentRepository.save(reagent);
        log.info("Successfully installed reagent id {} onto instrument {}", savedReagent.getId(), instrumentId);
        return InstrumentMapper.toReagentResponse(savedReagent);
    }
    @Override
    @Transactional
    public InstrumentReagentResponse modifyReagentStatus(String instrumentId, String reagentId, ModifyReagentStatusRequest request) {
        log.info("Modifying reagent status for reagent {} on instrument {}", reagentId, instrumentId);

        // 1. Tìm hóa chất (đảm bảo nó thuộc đúng thiết bị)
        InstrumentReagent reagent = instrumentReagentRepository.findByIdAndInstrumentId(reagentId, instrumentId)
                .orElseThrow(() -> new NotFoundException("Reagent not found with id " + reagentId + " on instrument " + instrumentId));

        // 2. Đảm bảo hóa chất chưa bị xóa (Req 3.6.2.2)
        if (reagent.isDeleted()) {
            throw new NotFoundException("Reagent not found with id " + reagentId + " on instrument " + instrumentId);
        }

        ReagentStatus newStatus = ReagentStatus.valueOf(request.getStatus());

        // 3. Kiểm tra việc cập nhật trùng lặp (Req 3.6.2.2)
        if (reagent.getStatus() == newStatus) {
            throw new IllegalArgumentException("Reagent status is already " + newStatus);
        }

        // 4. Cập nhật trạng thái
        reagent.setStatus(newStatus);
        InstrumentReagent updatedReagent = instrumentReagentRepository.save(reagent);

        // 5. Ghi log kiểm toán (Req 3.6.2.2)
        Map<String, Object> details = Map.of(
                "instrumentId", instrumentId,
                "reagentName", updatedReagent.getReagentName(),
                "lotNumber", updatedReagent.getLotNumber(),
                "oldStatus", reagent.getStatus().name(),
                "newStatus", newStatus.name()
        );
        auditLogService.logAction(AuditAction.MODIFY_REAGENT, updatedReagent.getId(), "InstrumentReagent", details);

        log.info("Successfully modified reagent status for id {} to {}", updatedReagent.getId(), newStatus);

        // 6. Trả về DTO Response
        return InstrumentMapper.toReagentResponse(updatedReagent);
    }

    @Override
    public void handleConfigurationCreation(ConfigurationCreatedEvent event) { // <-- Thêm
        String configId = event.getId();
        log.info("Handling ConfigurationCreatedEvent for ID: {}", configId);

        // Kiểm tra xem ID đã tồn tại chưa
        if (configurationRepository.existsById(configId)) {
            log.warn("Configuration (sync) with ID: {} already exists. Skipping creation.", configId);
            return;
        }

        // --- Xử lý Mapping ---
        // Do model 2 bên khác nhau, ta map các trường của warehouse_service
        // vào trường 'settings' của instrument_service.
        Map<String, Object> settings = new HashMap<>();
        settings.put("dataType", event.getDataType());
        settings.put("value", event.getValue());
        settings.put("description", event.getDescription());

        // Tạo entity Configuration của instrument_service
        Configuration newConfig = new Configuration();
        newConfig.setId(event.getId());
        newConfig.setName(event.getName());
        newConfig.setSettings(settings);

        // Các trường (configType, instrumentModel, v.v.) không có trong sự kiện
        // sẽ được giữ là null (mặc định).

        configurationRepository.save(newConfig);
        log.info("Successfully created configuration (sync) with ID: {}", configId);
    }

    @Override
    public void handleConfigurationDeletion(ConfigurationDeletedEvent event) {
        String configId = event.getConfigurationId();
        log.info("Handling ConfigurationDeletedEvent for ID: {}", configId);

        // Tìm và xóa configuration trong DB của instrument_service
        configurationRepository.findById(configId).ifPresent(config -> {
            configurationRepository.delete(config);
            log.info("Successfully deleted configuration (sync) with ID: {}", configId);
        });

        if (!configurationRepository.existsById(configId)) {
            log.warn("Configuration (sync) with ID: {} not found. No action taken.", configId);
        }
    }
}
