/*
 * @ {#} InstrumentServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import feign.FeignException;
import fit.instrument_service.client.WarehouseFeignClient;
import fit.instrument_service.client.dtos.ReagentLotStatusResponse;
import fit.instrument_service.configs.RabbitMQConfig;
import fit.instrument_service.dtos.request.ChangeInstrumentModeRequest;
import fit.instrument_service.dtos.request.InstallReagentRequest;
import fit.instrument_service.dtos.request.ModifyReagentStatusRequest;
import fit.instrument_service.dtos.request.ReagentInstallationDeductionRequest;
import fit.instrument_service.dtos.response.*;
import fit.instrument_service.embedded.Vendor;
import fit.instrument_service.entities.Configuration;
import fit.instrument_service.entities.Instrument;
import fit.instrument_service.entities.InstrumentModeLog;
import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.AuditAction;
import fit.instrument_service.enums.InstrumentMode;
import fit.instrument_service.enums.InstrumentStatus;
import fit.instrument_service.enums.ReagentStatus;
import fit.instrument_service.events.*;
import fit.instrument_service.enums.*;
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
import fit.instrument_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

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

        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with id: " + instrumentId));

        // 1. Lấy thông tin Vendor để lưu trữ (Vẫn cần call này để lấy tên Vendor)
        VendorResponse vendorDetails;
        try {
            ApiResponse<VendorResponse> vendorRes = warehouseFeignClient.getVendorById(request.getVendorId());

            if (vendorRes == null || !vendorRes.isSuccess() || vendorRes.getData() == null) {
                throw new NotFoundException("Vendor not found in warehouse with id: " + request.getVendorId());
            }
            vendorDetails = vendorRes.getData();

        } catch (FeignException.NotFound e) {
            // Bắt lỗi 404 từ Feign Client và ném ra NotFoundException của service hiện tại
            log.warn("Vendor ID {} not found in Warehouse Service", request.getVendorId());
            throw new NotFoundException("Vendor not found with ID: " + request.getVendorId());
        } catch (Exception e) {
            log.error("Error fetching vendor: {}", e.getMessage());
            throw new RuntimeException("Unable to fetch vendor details: " + e.getMessage());
        }

        // 2. GỌI WAREHOUSE ĐỂ TRỪ KHO (Thay thế bước validation cũ)
        try {
            // Tạo request DTO
            ReagentInstallationDeductionRequest deductReq = new ReagentInstallationDeductionRequest();
            deductReq.setLotNumber(request.getLotNumber());
            deductReq.setQuantity((double) request.getQuantity()); // Cast Integer sang Double
            deductReq.setInstrumentId(instrumentId);

            log.info("Requesting warehouse deduction for lot: {}", request.getLotNumber());

            // Thực hiện trừ kho
            ApiResponse<Boolean> deductRes = warehouseFeignClient.deductReagentForInstallation(deductReq);

            if (deductRes == null || !deductRes.isSuccess() || Boolean.FALSE.equals(deductRes.getData())) {
                throw new IllegalArgumentException("Failed to deduct reagent from warehouse. Insufficient stock or Invalid Lot.");
            }
            log.info("Warehouse deduction successful.");

        } catch (FeignException e) {
            log.error("Warehouse deduction failed: {}", e.getMessage());
            throw new IllegalArgumentException("Warehouse Error: " + e.contentUTF8());
        }

        // 3. LƯU THÔNG TIN VÀO INSTRUMENT SERVICE (Local Cache để dùng dần)

        // Kiểm tra trùng lặp logic cũ...
        List<InstrumentReagent> existing = instrumentReagentRepository
                .findByInstrumentIdAndLotNumberAndIsDeletedFalse(instrumentId, request.getLotNumber());
        if (!existing.isEmpty()) {
            // Nếu đã tồn tại, có thể bạn muốn cộng dồn số lượng hoặc báo lỗi.
            // Ở đây giả sử báo lỗi hoặc xử lý tùy nghiệp vụ.
            // Nếu logic là cộng dồn: existing.get(0).setQuantity(existing.get(0).getQuantity() + request.getQuantity());
            throw new IllegalArgumentException("This reagent lot is already installed on the instrument.");
        }

        InstrumentReagent reagent = new InstrumentReagent();
        reagent.setInstrumentId(instrumentId);
        reagent.setReagentName(request.getReagentName());
        reagent.setLotNumber(request.getLotNumber());

        // QUAN TRỌNG: Số lượng này bây giờ thuộc về Instrument, đã tách khỏi Warehouse
        reagent.setQuantity(request.getQuantity());

        reagent.setExpirationDate(request.getExpirationDate());
        reagent.setVendor(new Vendor(
                vendorDetails.getId(),
                vendorDetails.getName(),
                vendorDetails.getContactPerson()
        ));
        reagent.setStatus(ReagentStatus.NOT_IN_USE); // Mặc định chưa dùng ngay
        reagent.setDeleted(false);
        reagent.assignBusinessId();

        InstrumentReagent savedReagent = instrumentReagentRepository.save(reagent);

        // Log audit
        Map<String, Object> details = Map.of(
                "lotNumber", request.getLotNumber(),
                "quantity", request.getQuantity(),
                "action", "INSTALL_FROM_WAREHOUSE"
        );
        auditLogService.logAction(AuditAction.INSTALL_REAGENT, savedReagent.getId(), "InstrumentReagent", details);

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

        // Nếu Config đã tồn tại → bỏ qua
        if (configurationRepository.existsById(configId)) {
            log.warn("Configuration (sync) with ID: {} already exists. Skipping creation.", configId);
            return;
        }

        // --- Mapping settings ---
        // Vì Warehouse gửi settings = Map<String, Object>
        // nên ta giữ nguyên 100% (không cần bóc dataType/value nữa)
        Map<String, Object> settings = event.getSettings();

        // --- Tạo Configuration mới ---
        Configuration newConfig = new Configuration();
        newConfig.setId(event.getId());
        newConfig.setName(event.getName());

        // Map configType (String) → enum ConfigurationType
        if (event.getConfigType() != null) {
            try {
                newConfig.setConfigType(ConfigurationType.valueOf(event.getConfigType().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid configType '{}' — defaulting to null", event.getConfigType());
                newConfig.setConfigType(null);
            }
        }

        // Map các trường khác
        newConfig.setInstrumentModel(event.getInstrumentModel());
        newConfig.setInstrumentType(event.getInstrumentType());
        newConfig.setVersion(event.getVersion());

        // Settings JSON từ event (Map)
        newConfig.setSettings(settings);

        // Lưu vào MongoDB
        configurationRepository.save(newConfig);

        log.info("Successfully created configuration (sync) with ID: {}", configId);
    }

    @Override
    public void handleConfigurationDeletion(ConfigurationDeletedEvent event) {
        String configId = event.getConfigurationId();
        log.info("Handling ConfigurationDeletedEvent for ID: {}", configId);

        // Tìm configuration trong DB của instrument_service
        configurationRepository.findById(configId).ifPresentOrElse(config -> {
            config.setDeleted(true); // Lombok sinh setter là setDeleted cho field boolean isDeleted
            config.setDeletedAt(LocalDateTime.now());

            configurationRepository.save(config); // Lưu lại thay đổi cập nhật trạng thái

            log.info("Successfully soft-deleted configuration (sync) with ID: {}", configId);
        }, () -> {
            log.warn("Configuration (sync) with ID: {} not found. No action taken.", configId);
        });
    }

    @Override
    @Transactional
    public void handleConfigurationUpdate(ConfigurationUpdatedEvent event) {
        String configId = event.getId();
        log.info("Handling ConfigurationUpdatedEvent for ID: {}", configId);

        // 1. Tìm cấu hình trong DB của instrument_service
        Optional<Configuration> existingConfigOpt = configurationRepository.findById(configId);

        if (existingConfigOpt.isEmpty()) {
            log.warn("Configuration (sync) with ID: {} not found. Update skipped.", configId);
            return;
        }

        Configuration existingConfig = existingConfigOpt.get();

        // 2. Cập nhật thông tin từ Event
        // Lưu ý: Chỉ cập nhật các trường có thể thay đổi từ Warehouse Service
        boolean isUpdated = false;

        if (event.getVersion() != null && !event.getVersion().equals(existingConfig.getVersion())) {
            existingConfig.setVersion(event.getVersion());
            isUpdated = true;
        }

        if (event.getSettings() != null) {
            existingConfig.setSettings(event.getSettings());
            isUpdated = true;
        }

        // Nếu tên có thay đổi (tùy chọn)
        if (event.getName() != null && !event.getName().equals(existingConfig.getName())) {
            existingConfig.setName(event.getName());
            isUpdated = true;
        }

        // 3. Lưu lại nếu có thay đổi
        if (isUpdated) {
            existingConfig.setUpdatedAt(LocalDateTime.now());
            configurationRepository.save(existingConfig);
            log.info("Successfully updated configuration (sync) with ID: {}", configId);
        } else {
            log.info("No changes detected for configuration (sync) with ID: {}", configId);
        }
    }

    @Override
    public SyncConfigurationResponse syncUpConfiguration(String instrumentId) {
        // Tìm Instrument
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with id: " + instrumentId));

        // Kiểm tra model/type
        if (!StringUtils.hasText(instrument.getType()) || !StringUtils.hasText(instrument.getModel())) {
            throw new IllegalArgumentException("Instrument model/type is required to sync configurations");
        }

        // Tìm cấu hình chung và riêng
        List<String> warnings = new ArrayList<>();

        // Cấu hình chung
        Optional<Configuration> generalConfigOpt = configurationRepository
                .findTopByConfigTypeOrderByVersionDesc(ConfigurationType.GENERAL);

        // Cấu hình chung không tìm thấy
        Configuration generalConfig = generalConfigOpt.orElse(null);
        if (generalConfig == null) {
            String warning = "General configuration is missing and could not be applied.";
            warnings.add(warning);
            log.warn("{} Instrument ID: {}", warning, instrumentId);
        }

        // Cấu hình riêng
        Optional<Configuration> specificConfigOpt = configurationRepository
                .findTopByConfigTypeAndInstrumentModelAndInstrumentTypeOrderByVersionDesc(
                        ConfigurationType.SPECIFIC,
                        instrument.getModel(),
                        instrument.getType()
                );

        // Nếu không tìm thấy theo model + type, thử chỉ theo type
        if (specificConfigOpt.isEmpty()) {
            specificConfigOpt = configurationRepository
                    .findTopByConfigTypeAndInstrumentTypeOrderByVersionDesc(
                            ConfigurationType.SPECIFIC,
                            instrument.getType()
                    );
        }

        // Cấu hình riêng không tìm thấy
        Configuration specificConfig = specificConfigOpt.orElse(null);

        // Nếu vẫn không tìm thấy, ghi cảnh báo
        if (specificConfig == null) {
            String warning = "Specific configuration matching instrument type/model is missing.";
            warnings.add(warning);
            log.warn("{} Instrument ID: {}, type: {}, model: {}", warning, instrumentId, instrument.getType(), instrument.getModel());
        }

        // Áp dụng cấu hình chung
        Map<String, Object> generalSettings = generalConfig != null && generalConfig.getSettings() != null
                ? new HashMap<>(generalConfig.getSettings())
                : new HashMap<>();

        // Áp dụng cấu hình riêng
        Map<String, Object> specificSettings = specificConfig != null && specificConfig.getSettings() != null
                ? new HashMap<>(specificConfig.getSettings())
                : new HashMap<>();

        // Kết hợp cấu hình chung + riêng
        Map<String, Object> appliedSettings = new HashMap<>();
        appliedSettings.putAll(generalSettings);
        appliedSettings.putAll(specificSettings);

        // Cờ đồng bộ đầy đủ
        boolean fullySynced = generalConfig != null && specificConfig != null;

        // Ghi log kiểm toán nếu đồng bộ đầy đủ
        if (fullySynced) {
            Map<String, Object> auditDetails = Map.of(
                    "performedBy", SecurityUtils.getCurrentUserId(),
                    "generalConfigId", generalConfig.getId(),
                    "specificConfigId", specificConfig.getId(),
                    "appliedAt", LocalDateTime.now()
            );
            auditLogService.logAction(AuditAction.SYNC_UP_CONFIGURATION, instrumentId, "Instrument", auditDetails);
        }

        return SyncConfigurationResponse.builder()
                .instrumentId(instrumentId)
                .instrumentModel(instrument.getModel())
                .instrumentType(instrument.getType())
                .generalConfigId(generalConfig != null ? generalConfig.getId() : null)
                .specificConfigId(specificConfig != null ? specificConfig.getId() : null)
                .generalSettings(generalSettings.isEmpty() ? null : generalSettings)
                .specificSettings(specificSettings.isEmpty() ? null : specificSettings)
                .appliedSettings(appliedSettings.isEmpty() ? null : appliedSettings)
                .fullySynced(fullySynced)
                .warnings(warnings.isEmpty() ? null : warnings)
                .build();
    }
}
