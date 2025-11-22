/*
<<<<<<< HEAD
 * @ {#} InstrumentServiceImpl.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import fit.warehouse_service.constants.SortFields;
import fit.warehouse_service.dtos.request.ActivateInstrumentRequest;
import fit.warehouse_service.dtos.request.CheckInstrumentStatusRequest;
import fit.warehouse_service.dtos.request.CreateInstrumentRequest;
import fit.warehouse_service.dtos.request.DeactivateInstrumentRequest;
import fit.warehouse_service.dtos.response.*;
import fit.warehouse_service.entities.*;
import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.enums.ProtocolType;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.events.InstrumentActivatedEvent;
import fit.warehouse_service.events.InstrumentDeactivatedEvent;
import fit.warehouse_service.exceptions.AlreadyExistsException;
import fit.warehouse_service.exceptions.DuplicateResourceException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.exceptions.ResourceNotFoundException;
import fit.warehouse_service.mappers.InstrumentMapper;
import fit.warehouse_service.repositories.*;
import fit.warehouse_service.services.*;
import fit.warehouse_service.specifications.InstrumentSpecification;
import fit.warehouse_service.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description: Implementation of InstrumentService to handle instrument.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {
    private final InstrumentRepository instrumentRepository;

    private final WarehouseEventLogRepository warehouseEventLogRepository;

    private final Hl7StatusParserService hl7StatusParserService;

    private final Hl7Client hl7Client;

    private final ReagentTypeRepository reagentTypeRepository;

    private final ConfigurationSettingRepository configurationSettingRepository;

    private final WarehouseEventLogService logService;

    private final ScheduledDeletionService scheduledDeletionService;

    private final InstrumentMapper instrumentMapper;

    private final InstrumentSpecification instrumentSpecification;

    private final EventPublisherService eventPublisherService;

    private final VendorRepository vendorRepository;

    @Override
    @Transactional
    public Instrument createInstrument(CreateInstrumentRequest request) {
        // 1. Check for duplicate name
        instrumentRepository.findByName(request.getName()).ifPresent(ins -> {
            throw new DuplicateResourceException("Instrument with name '" + request.getName() + "' already exists.");
        });

        // Check for duplicate serial number
        if (instrumentRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new DuplicateResourceException("Serial number already exists: " + request.getSerialNumber());
        }

        // Check for duplicate IP and Port combination
        if (instrumentRepository.existsByIpAddressAndPort(request.getIpAddress(), request.getPort())) {
            throw new DuplicateResourceException(
                    String.format("Instrument with IP address '%s' and Port '%d' already exists.",
                            request.getIpAddress(), request.getPort())
            );
        }

        // Verify vendor exists
        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor with ID '" + request.getVendorId() + "' not found."));

        Instrument instrument = new Instrument();
        instrument.setName(request.getName());
        instrument.setStatus(InstrumentStatus.INACTIVE);
        instrument.setActive(false);
        instrument.setModel(request.getModel());
        instrument.setType(request.getType());
        instrument.setSerialNumber(request.getSerialNumber());
        instrument.setVendor(vendor);


        instrument.setIpAddress(request.getIpAddress());
        instrument.setPort(request.getPort());
        instrument.setProtocolType(request.getProtocolType() != null ? request.getProtocolType() : ProtocolType.HL7);

        Set<ReagentType> compatibleReagents = new HashSet<>();
        Set<ConfigurationSetting> configurations = new HashSet<>();

        // 2. Handle cloning logic
        if (request.getCloneFromInstrumentId() != null && !request.getCloneFromInstrumentId().isEmpty()) {
            Instrument sourceInstrument = instrumentRepository.findById(request.getCloneFromInstrumentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source Instrument for cloning not found."));
            // Clone only compatible settings, not network details like IP/Port
            compatibleReagents.addAll(sourceInstrument.getCompatibleReagents());
            configurations.addAll(sourceInstrument.getConfigurations());
        }
        // 3. Handle manual assignment logic
        else {
            if (request.getCompatibleReagentIds() != null && !request.getCompatibleReagentIds().isEmpty()) {
                Set<String> requestedReagentIds = request.getCompatibleReagentIds();
                List<ReagentType> foundReagents = reagentTypeRepository.findAllById(requestedReagentIds);

                if (foundReagents.size() != requestedReagentIds.size()) {
                    // Tìm các ID không tồn tại để báo lỗi
                    Set<String> foundIds = foundReagents.stream().map(ReagentType::getId).collect(Collectors.toSet());
                    Set<String> missingIds = new HashSet<>(requestedReagentIds);
                    missingIds.removeAll(foundIds);
                    throw new ResourceNotFoundException("Could not find ReagentType IDs: " + missingIds);
                }
                compatibleReagents.addAll(foundReagents);
            }

            if (request.getConfigurationSettingIds() != null && !request.getConfigurationSettingIds().isEmpty()) {
                Set<String> requestedConfigIds = request.getConfigurationSettingIds();
                List<ConfigurationSetting> foundConfigs = configurationSettingRepository.findAllById(requestedConfigIds);

                if (foundConfigs.size() != requestedConfigIds.size()) {
                    // Tìm các ID không tồn tại để báo lỗi
                    Set<String> foundIds = foundConfigs.stream().map(ConfigurationSetting::getId).collect(Collectors.toSet());
                    Set<String> missingIds = new HashSet<>(requestedConfigIds);
                    missingIds.removeAll(foundIds);
                    throw new ResourceNotFoundException("Could not find ConfigurationSetting IDs: " + missingIds);
                }
                configurations.addAll(foundConfigs);
            }
        }

        instrument.setCompatibleReagents(compatibleReagents);
        instrument.setConfigurations(configurations);

        // 4. Save instrument (Auditing fields are handled automatically)
        Instrument savedInstrument = instrumentRepository.save(instrument);

        // 5. Log the action
        String logDetails = logService.createInstrumentCreatedDetails(savedInstrument);
        logService.logEvent(
                WarehouseActionType.INSTRUMENT_CREATED,
                savedInstrument.getId(),
                "Instrument",
                logDetails
        );

        // 6. Return saved entity
        return savedInstrument;
    }

    @Override
    @Transactional
    public InstrumentActivationResponse activateInstrument(ActivateInstrumentRequest request) {
        // Tìm kiếm thiết bị dựa trên ID
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException(
                        "Instrument not found with ID: " + request.getInstrumentId()));

        // Kiểm tra nếu thiết bị đã được kích hoạt
        if (instrument.isActive()) {
            throw new AlreadyExistsException(
                    "Instrument '" + instrument.getName() + "' is already active");
        }

        // Lưu trạng thái trước khi kích hoạt để ghi log
        boolean previousActiveState = instrument.isActive();
        InstrumentStatus previousStatus = instrument.getStatus();

        // Kích hoạt thiết bị
        instrument.setActive(true);

        // Nếu thiết bị đang ở trạng thái INACTIVE, thực hiện kiểm tra lại để xác định trạng thái phù hợp
        if (instrument.getStatus() == InstrumentStatus.INACTIVE) {
            // Thực hiện kiểm tra lại trạng thái thiết bị
            InstrumentStatus newStatus = performInstrumentRecheck(instrument);
            instrument.setStatus(newStatus);
        }

        // Lưu thiết bị đã được cập nhật
        Instrument savedInstrument = instrumentRepository.save(instrument);

        // Phát sự kiện kích hoạt thiết bị sang instrument-service
        InstrumentActivatedEvent event = InstrumentActivatedEvent.builder()
                .id(savedInstrument.getId())
                .name(savedInstrument.getName())
                .model(savedInstrument.getModel())
                .type(savedInstrument.getType())
                .serialNumber(savedInstrument.getSerialNumber())
                .vendorId(savedInstrument.getVendor().getId())
                .vendorName(savedInstrument.getVendor().getName())
                .vendorContact(savedInstrument.getVendor().getPhone())
                .build();

        eventPublisherService.publishInstrumentActivated(event);

        // Hủy lịch xóa đã lên lịch (nếu có)
        scheduledDeletionService.cancelScheduledDeletion(savedInstrument.getId());

        // Ghi log sự kiện kích hoạt
        String logDetails = logService.createInstrumentActivationDetails(
                savedInstrument, "ACTIVATED", request.getReason(), previousActiveState, previousStatus);
        logService.logEvent(
                WarehouseActionType.INSTRUMENT_ACTIVATED,
                savedInstrument.getId(),
                "Instrument",
                logDetails
        );

        log.info("Instrument {} has been activated. Status: {}",
                savedInstrument.getId(), savedInstrument.getStatus());

        return InstrumentActivationResponse.builder()
                .instrumentId(savedInstrument.getId())
                .instrumentName(savedInstrument.getName())
                .isActive(savedInstrument.isActive())
                .currentStatus(savedInstrument.getStatus())
                .actionPerformed("ACTIVATED")
                .reason(request.getReason())
                .actionTimestamp(LocalDateTime.now())
                .message("Instrument has been successfully activated and is now available for use")
                .canBeUsedForTestOrders(canInstrumentBeUsed(savedInstrument))
                .build();
    }

    @Transactional
    @Override
    public InstrumentActivationResponse deactivateInstrument(DeactivateInstrumentRequest request) {
        // Tìm kiếm thiết bị dựa trên ID
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException(
                        "Instrument not found with ID: " + request.getInstrumentId()));

        // Kiểm tra nếu thiết bị đã bị vô hiệu hóa
        if (!instrument.isActive()) {
            throw new AlreadyExistsException(
                    "Instrument '" + instrument.getName() + "' is already inactive");
        }

        // Lưu trạng thái trước khi vô hiệu hóa để ghi log
        boolean previousActiveState = instrument.isActive();
        InstrumentStatus previousStatus = instrument.getStatus();

        // Vô hiệu hóa thiết bị
        instrument.setActive(false);

        // Cập nhật trạng thái thiết bị thành INACTIVE
        instrument.setStatus(InstrumentStatus.INACTIVE);

        // Lưu thiết bị đã được cập nhật
        Instrument savedInstrument = instrumentRepository.save(instrument);

        // Phát sự kiện vô hiệu hóa thiết bị sang instrument-service
        InstrumentDeactivatedEvent event = InstrumentDeactivatedEvent.builder()
                .id(savedInstrument.getId())
                .build();

        eventPublisherService.publishInstrumentDeactivated(event);

        // Lên lịch xóa thiết bị sau 3 tháng
        scheduledDeletionService.scheduleInstrumentDeletion(
                savedInstrument.getId(),
                request.getReason()
        );

        // Ghi log sự kiện vô hiệu hóa
        String logDetails = logService.createInstrumentActivationDetails(
                savedInstrument, "DEACTIVATED", request.getReason(), previousActiveState, previousStatus);
        logService.logEvent(
                WarehouseActionType.INSTRUMENT_DEACTIVATED,
                savedInstrument.getId(),
                "Instrument",
                logDetails
        );

        log.info("Instrument {} has been deactivated. Status: INACTIVE", savedInstrument.getId());

        return InstrumentActivationResponse.builder()
                .instrumentId(savedInstrument.getId())
                .instrumentName(savedInstrument.getName())
                .isActive(savedInstrument.isActive())
                .currentStatus(savedInstrument.getStatus())
                .actionPerformed("DEACTIVATED")
                .reason(request.getReason())
                .actionTimestamp(LocalDateTime.now())
                .message("Instrument has been successfully deactivated and is now unavailable for test orders")
                .canBeUsedForTestOrders(canInstrumentBeUsed(savedInstrument))
                .build();
    }

    @Transactional
    @Override
    public InstrumentStatusResponse checkInstrumentStatus(CheckInstrumentStatusRequest request) {
        // Tìm kiếm thiết bị dựa trên ID
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException(
                        "Instrument not found with ID: " + request.getInstrumentId()));

        // Lưu trạng thái trước khi kiểm tra lại
        InstrumentStatus previousStatus = instrument.getStatus();

        // Chuẩn bị biến để theo dõi việc kiểm tra lại
        boolean recheckPerformed = false;

        // Tạo statusMessage trạng thái ban đầu
        String statusMessage = generateStatusMessage(instrument.getStatus());

        // Chi tiết lỗi (nếu có)
        String errorDetails = null;

        // Thực hiện kiểm tra lại nếu thiết bị ở trạng thái LỖI hoặc yêu cầu kiểm tra lại
        if (instrument.getStatus() == InstrumentStatus.ERROR || request.isForceRecheck()) {
            log.info("Performing recheck for instrument: {} with status: {}",
                    instrument.getId(), instrument.getStatus());

            // Thực hiện kiểm tra lại
            recheckPerformed = true;

            // Kiểm tra lại trạng thái thiết bị
            InstrumentStatus newStatus = performInstrumentRecheck(instrument);

            // Cập nhật trạng thái thiết bị nếu có sự thay đổi
            if (newStatus != previousStatus) {
                instrument.setStatus(newStatus);
                instrumentRepository.save(instrument);

                // Ghi log sự thay đổi trạng thái
                logStatusChange(instrument, previousStatus, newStatus);

                log.info("Instrument {} status changed from {} to {}",
                        instrument.getId(), previousStatus, newStatus);
            }

            // Cập nhật thông điệp trạng thái mới
            statusMessage = generateStatusMessage(instrument.getStatus());

            // Nếu vẫn ở trạng thái LỖI, tạo chi tiết lỗi
            if (instrument.getStatus() == InstrumentStatus.ERROR) {
                errorDetails = generateErrorDetails(instrument);
            }
        }

        // Xây dựng và trả về phản hồi trạng thái thiết bị
        return InstrumentStatusResponse.builder()
                .instrumentId(instrument.getId())
                .instrumentName(instrument.getName())
                .currentStatus(instrument.getStatus())
                .previousStatus(recheckPerformed ? previousStatus : null)
                .isActive(instrument.isActive())
                .recheckPerformed(recheckPerformed)
                .lastCheckedAt(LocalDateTime.now())
                .statusMessage(statusMessage)
                .errorDetails(errorDetails)
                .canBeUsed(canInstrumentBeUsed(instrument))
                .build();
    }

    // Phương thức thực hiện kiểm tra lại thiết bị
    private InstrumentStatus performInstrumentRecheck(Instrument instrument) {
        try {
            String hl7Response = hl7Client.requestStatus(instrument);
            log.info("HL7 response for recheck of instrument {}: \n{}",
                    instrument.getId(), hl7Response);
            InstrumentStatus status = hl7StatusParserService.parseStatusFromHl7(hl7Response);
            return status;
        } catch (Exception e) {
            log.error("Recheck failed for instrument {}: {}", instrument.getId(), e.getMessage());
            return InstrumentStatus.ERROR;
        }
    }

    // Ghi log sự thay đổi trạng thái thiết bị
    private void logStatusChange(Instrument instrument, InstrumentStatus fromStatus, InstrumentStatus toStatus) {
        WarehouseEventLog log = new WarehouseEventLog();
        log.setAction(WarehouseActionType.INSTRUMENT_STATUS_UPDATED);
        log.setEntityType("Instrument");
        log.setEntityId(instrument.getId());
        log.setDetails(String.format("Status changed from %s to %s via recheck", fromStatus, toStatus));

        warehouseEventLogRepository.save(log);
    }

    // Tạo thông điệp trạng thái dựa trên trạng thái thiết bị
    private String generateStatusMessage(InstrumentStatus status) {
        return switch (status) {
            case READY -> "Instrument is ready for use";
            case PROCESSING -> "Instrument is currently processing";
            case MAINTENANCE -> "Instrument is under maintenance";
            case ERROR -> "Instrument has encountered an error and cannot be used";
            case INACTIVE -> "Instrument is inactive";
        };
    }

    @Override
    public List<Instrument> getAllInstruments() {
        log.info("Fetching all instruments from the database.");
        List<Instrument> instruments = instrumentRepository.findAll();

        instruments.sort(Comparator.comparing(
                (Instrument i) -> i.getUpdatedAt() != null ? i.getUpdatedAt() : i.getCreatedAt(),
                Comparator.nullsLast(Comparator.reverseOrder())
        ));


        log.info("Retrieved {} instruments.", instruments.size());
        return instruments;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InstrumentResponse> getAllInstruments(int page, int size, String[] sort, String search, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching all instruments with filters: search='{}', startDate='{}', endDate='{}', page={}",
                search, startDate, endDate, page);

        // Xây dựng đối tượng Sort hợp lệ
        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.INSTRUMENT_SORT_FIELDS,
                SortFields.DEFAULT_INSTRUMENT_SORT
        );

        Pageable pageable = PageRequest.of(page, size, validSort);

        // Xây dựng Specification dựa trên các tiêu chí lọc
        Specification<Instrument> spec = instrumentSpecification.build(search, startDate, endDate);

        // Truy vấn CSDL với phân trang và lọc
        Page<Instrument> pageInstruments = instrumentRepository.findAll(spec, pageable);

        // Chuyển đổi Page<Entity> sang Page<DTO>
        Page<InstrumentResponse> dtoPage = pageInstruments.map(instrumentMapper::toResponse);

        // Tạo đối tượng FilterInfo để trả về thông tin lọc (sử dụng lại ConfigurationFilterInfo theo yêu cầu)
        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .startDate(startDate)
                .endDate(endDate)
                .dataType(null) // Như yêu cầu, không bao gồm dataType
                .build();

        log.info("Retrieved {} instruments.", dtoPage.getTotalElements());

        return PageResponse.from(dtoPage, filterInfo);
    }

    // Tạo chi tiết lỗi cho thiết bị ở trạng thái LỖI
    private String generateErrorDetails(Instrument instrument) {
        return String.format(
                "Instrument '%s' (ID: %s) is experiencing technical difficulties. " +
                        "Possible issues: Hardware malfunction, calibration required, or configuration error. " +
                        "Please contact technical support for assistance.",
                instrument.getName(),
                instrument.getId()
        );
    }

    // Kiểm tra xem thiết bị có thể được sử dụng hay không
    private boolean canInstrumentBeUsed(Instrument instrument) {
        return instrument.isActive() &&
                instrument.getStatus() == InstrumentStatus.READY;
    }
}
