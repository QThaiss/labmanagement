/*
 * @ (#) ReagentHistoryServiceImpl.java    1.0    30/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.services.impl;

import fit.warehouse_service.dtos.request.LogReagentUsageRequest;
import fit.warehouse_service.dtos.request.ReceiveReagentRequest;
import fit.warehouse_service.dtos.response.ReagentLotStatusResponse;
import fit.warehouse_service.dtos.response.ReagentSupplyHistoryResponse;
import fit.warehouse_service.dtos.response.ReagentUsageHistoryResponse;
import fit.warehouse_service.entities.*;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.mappers.ReagentSupplyHistoryMapper;
import fit.warehouse_service.mappers.ReagentUsageHistoryMapper;
import fit.warehouse_service.repositories.*;
import fit.warehouse_service.services.ReagentHistoryService;
import fit.warehouse_service.services.WarehouseEventLogService;
import fit.warehouse_service.utils.SecurityUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import StringUtils

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper


@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentHistoryServiceImpl implements ReagentHistoryService {

    private final ReagentSupplyHistoryRepository historyRepository;
    private final ReagentTypeRepository reagentTypeRepository;
    private final ReagentSupplyHistoryMapper historyMapper;
    private final WarehouseEventLogService logService; // Inject Log Service
    private final ObjectMapper objectMapper; // Inject ObjectMapper

    private final ReagentUsageHistoryRepository usageHistoryRepository;
    private final ReagentLotRepository reagentLotRepository;
    private final InstrumentRepository instrumentRepository;
    private final ReagentUsageHistoryMapper usageHistoryMapper;
    private final VendorRepository vendorRepository;

    @Override
    @Transactional
    public ReagentSupplyHistoryResponse receiveReagentShipment(ReceiveReagentRequest request) {
        log.info("Receiving reagent shipment for PO: {}, Lot: {}", request.getPoNumber(), request.getLotNumber());

        // 1. Validate ReagentType exists
        ReagentType reagentType = reagentTypeRepository.findById(request.getReagentTypeId())
                .orElseThrow(() -> new NotFoundException("ReagentType not found with ID: " + request.getReagentTypeId()));

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new NotFoundException("Vendor not found with ID: " + request.getVendorId()));

        // 2. Create History Entity
        ReagentSupplyHistory history = new ReagentSupplyHistory();
        history.setReagentType(reagentType);
        history.setVendor(vendor);
        history.setPoNumber(request.getPoNumber());
        history.setOrderDate(request.getOrderDate());
        history.setReceiptDate(request.getReceiptDate()); // User-provided date
        history.setQuantityReceived(request.getQuantityReceived());
        history.setUnitOfMeasure(request.getUnitOfMeasure());
        history.setLotNumber(request.getLotNumber());
        history.setExpirationDate(request.getExpirationDate());
        history.setReceivedByUserId(SecurityUtils.getCurrentUserId()); // Explicit receiver
        history.setInitialStorageLocation(request.getInitialStorageLocation());
        history.setStatus(request.getStatus());

        // BaseEntity fields (createdAt, createdByUserId) will be set automatically by Auditing

        // 3. Save History (Immutable Record)
        ReagentSupplyHistory savedHistory = historyRepository.save(history);
        log.info("Saved reagent supply history with ID: {}", savedHistory.getId());

        // 4. Log the REAGENT_RECEIVED event
        String logDetails = createReagentReceivedDetails(savedHistory); // Use helper method
        logService.logEvent(
                WarehouseActionType.REAGENT_RECEIVED,
                savedHistory.getId(),
                "ReagentSupplyHistory", // Or "ReagentLot" depending on primary focus
                logDetails
        );

        return historyMapper.toResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReagentSupplyHistoryResponse> getReagentSupplyHistory(
            String reagentTypeId, String vendorId, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        log.info("Fetching reagent supply history with filters - ReagentType: {}, Vendor: {}, StartDate: {}, EndDate: {}, Page: {}",
                reagentTypeId, vendorId, startDate, endDate, pageable);

        Specification<ReagentSupplyHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(reagentTypeId)) {
                predicates.add(cb.equal(root.get("reagentType").get("id"), reagentTypeId));
            }

            if (StringUtils.hasText(vendorId)) {
                // Assuming vendorId is stored directly. Adjust if it's a relation.
                predicates.add(cb.equal(root.get("vendorId"), vendorId));
            }

            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));

            }
            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }


            predicates.add(cb.equal(root.get("isDeleted"), false));


            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Fetch data using the specification and pageable
        Page<ReagentSupplyHistory> historyPage = historyRepository.findAll(spec, pageable);

        // Map the Page<Entity> to Page<DTO>
        return historyPage.map(historyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReagentSupplyHistoryResponse getReagentSupplyHistoryById(String historyId) {
        log.info("Fetching reagent supply history by ID: {}", historyId);
        ReagentSupplyHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new NotFoundException("Reagent Supply History record not found with ID: " + historyId));

        // Ensure it's not soft-deleted if applicable
        if (history.isDeleted()) {
            throw new NotFoundException("Reagent Supply History record not found with ID: " + historyId);
        }

        return historyMapper.toResponse(history);
    }


    // Helper method to create JSON details for logging
    private String createReagentReceivedDetails(ReagentSupplyHistory history) {
        try {
            Map<String, Object> details = Map.ofEntries(
                    Map.entry("historyId", history.getId()),
                    Map.entry("reagentTypeId", history.getReagentType() != null ? history.getReagentType().getId() : "N/A"),
                    Map.entry("reagentName", history.getReagentType() != null ? history.getReagentType().getName() : "N/A"),
                    Map.entry("lotNumber", history.getLotNumber()),
                    Map.entry("quantityReceived", history.getQuantityReceived()),
                    Map.entry("unitOfMeasure", history.getUnitOfMeasure()),
                    Map.entry("vendorName", history.getVendor() != null ? history.getVendor().getName() : "N/A"),
                    Map.entry("poNumber", history.getPoNumber()),
                    Map.entry("receiptDate", history.getReceiptDate() != null ? history.getReceiptDate().toString() : "N/A"),
                    Map.entry("expirationDate", history.getExpirationDate() != null ? history.getExpirationDate().toString() : "N/A"),
                    Map.entry("receivedByUserId", history.getReceivedByUserId()), // Người nhận (do người dùng nhập)
                    Map.entry("initialStorageLocation", history.getInitialStorageLocation()),
                    Map.entry("status", history.getStatus().name()),
                    Map.entry("loggedByUserId", history.getCreatedByUserId()), // Người ghi log (từ BaseEntity)
                    Map.entry("loggedAt", history.getCreatedAt() != null ? history.getCreatedAt().toString() : "N/A") // Thời gian ghi log (từ BaseEntity)
            );


            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize reagent received details for logging history ID {}: {}", history.getId(), e.getMessage());
            return "{\"error\": \"Could not serialize details\"}";
        }
    }
    @Override
    @Transactional
    public ReagentUsageHistoryResponse logReagentUsage(LogReagentUsageRequest request) {
        log.info("Logging reagent usage for Lot ID: {}, Quantity: {}, Action: {}",
                request.getReagentLotId(), request.getQuantityUsed(), request.getAction());

        // 1. Tìm Lô hóa chất (ReagentLot)
        ReagentLot reagentLot = reagentLotRepository.findById(request.getReagentLotId())
                .orElseThrow(() -> new NotFoundException("ReagentLot not found with ID: " + request.getReagentLotId()));

        // 2. Tìm Thiết bị (Instrument)
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException("Instrument not found with ID: " + request.getInstrumentId()));

        // 3. Kiểm tra số lượng tồn kho
        if (reagentLot.getCurrentQuantity() < request.getQuantityUsed()) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient quantity for ReagentLot ID: %s. Requested: %.2f, Available: %.2f",
                    request.getReagentLotId(), request.getQuantityUsed(), reagentLot.getCurrentQuantity()
            ));
        }

        // 4. Cập nhật số lượng tồn kho của Lô
        reagentLot.setCurrentQuantity(reagentLot.getCurrentQuantity() - request.getQuantityUsed());
        reagentLotRepository.save(reagentLot);
        log.info("Updated quantity for ReagentLot {}. New quantity: {}", reagentLot.getId(), reagentLot.getCurrentQuantity());

        // 5. Tạo bản ghi lịch sử (Immutable Record)
        ReagentUsageHistory usageHistory = new ReagentUsageHistory();
        usageHistory.setReagentLot(reagentLot);
        usageHistory.setInstrument(instrument);
        usageHistory.setQuantityUsed(request.getQuantityUsed());
        usageHistory.setAction(request.getAction().toUpperCase());
        // Auditing (id, createdAt, createdByUserId) sẽ tự động được điền khi lưu

        ReagentUsageHistory savedHistory = usageHistoryRepository.save(usageHistory);
        log.info("Saved reagent usage history with ID: {}", savedHistory.getId());

        // 6. Ghi log sự kiện
        String logDetails = logService.createReagentUsageDetails(savedHistory);
        logService.logEvent(
                WarehouseActionType.REAGENT_USED,
                savedHistory.getId(),
                "ReagentUsageHistory",
                logDetails
        );

        // 7. Trả về DTO chi tiết
        return usageHistoryMapper.toResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReagentUsageHistoryResponse> getReagentUsageHistory(String reagentTypeId, String reagentLotId, Pageable pageable) {

        log.info("Fetching reagent usage history with filters - ReagentType: {}, ReagentLot: {}, Page: {}",
                reagentTypeId, reagentLotId, pageable);

        Specification<ReagentUsageHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo ReagentLot ID (Trực tiếp)
            if (StringUtils.hasText(reagentLotId)) {
                predicates.add(cb.equal(root.get("reagentLot").get("id"), reagentLotId));
            }
            // Lọc theo ReagentType ID (Yêu cầu Join)
            else if (StringUtils.hasText(reagentTypeId)) {
                Join<ReagentUsageHistory, ReagentLot> lotJoin = root.join("reagentLot");
                Join<ReagentLot, ReagentType> typeJoin = lotJoin.join("reagentType");
                predicates.add(cb.equal(typeJoin.get("id"), reagentTypeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ReagentUsageHistory> historyPage = usageHistoryRepository.findAll(spec, pageable);


        return historyPage.map(usageHistoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkReagentStockExists(String vendorId, String lotNumber) {
        log.info("Validating stock for Vendor ID: {} and Lot Number: {}", vendorId, lotNumber);

        return historyRepository.existsByVendorIdAndLotNumber(vendorId, lotNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public ReagentLotStatusResponse getReagentLotStatus(String lotNumber) {
        log.info("Fetching lot status for LotNumber: {}", lotNumber);

        // Tìm lot bằng lotNumber
        ReagentLot reagentLot = reagentLotRepository.findByLotNumber(lotNumber);

        if (reagentLot == null) {
            throw new NotFoundException("ReagentLot not found with LotNumber: " + lotNumber);
        }

        // Trả về ID và số lượng hiện tại
        return new ReagentLotStatusResponse(reagentLot.getId(), reagentLot.getCurrentQuantity(),reagentLot.getReagentType().getUsagePerRun());
    }
}