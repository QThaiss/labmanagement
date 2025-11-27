package fit.warehouse_service.services.impl;

import fit.warehouse_service.dtos.request.ReagentDeductionRequest;
import fit.warehouse_service.dtos.request.ReagentInstallationDeductionRequest;
import fit.warehouse_service.dtos.response.ApiResponse;
import fit.warehouse_service.dtos.response.ReagentDeductionResponse;
import fit.warehouse_service.dtos.response.ReagentUsageLimit;
import fit.warehouse_service.entities.ReagentLot;
import fit.warehouse_service.entities.ReagentUsageHistory;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.exceptions.BadRequestException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.repositories.ReagentLotRepository;
import fit.warehouse_service.repositories.ReagentUsageHistoryRepository;
import fit.warehouse_service.services.ReagentService;
import fit.warehouse_service.services.WarehouseEventLogService;
import fit.warehouse_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentServiceImpl implements ReagentService {

    private final ReagentLotRepository reagentLotRepository;
    private final ReagentUsageHistoryRepository usageHistoryRepository;
    private final WarehouseEventLogService logService;

    private static final Map<String, ReagentUsageLimit> USAGE_LIMITS = Map.of(
            "Diluent", new ReagentUsageLimit(1.0, 2.0),        // ml
            "Lysing", new ReagentUsageLimit(50.0, 200.0),      // µL
            "Staining", new ReagentUsageLimit(50.0, 200.0),    // µL
            "Clotting", new ReagentUsageLimit(50.0, 100.0),    // µL
            "Cleaner", new ReagentUsageLimit(1.0, 2.0)         // ml
    );

    @Override
    @Transactional
    public ApiResponse<ReagentDeductionResponse> checkAndDeductReagent(ReagentDeductionRequest request) {
        log.info("Processing reagent deduction for Order {}: {} ({} ml)",
                request.getOrderId(), request.getReagentName(), request.getRequiredVolume());

        // 1. Tìm các lô hàng phù hợp (FEFO)
        List<ReagentLot> availableLots = reagentLotRepository.findAvailableLotsByName(
                request.getReagentName(), LocalDate.now());

        if (availableLots.isEmpty()) {
            return ApiResponse.success(ReagentDeductionResponse.builder()
                    .deductionSuccessful(false)
                    .message("No available lots found for reagent: " + request.getReagentName())
                    .deductedVolume(0)
                    .build(), "Failed");
        }

        // 2. Tính tổng lượng tồn
        double totalAvailable = availableLots.stream().mapToDouble(ReagentLot::getCurrentQuantity).sum();
        if (totalAvailable < request.getRequiredVolume()) {
            return ApiResponse.success(ReagentDeductionResponse.builder()
                    .deductionSuccessful(false)
                    .message("Insufficient volume. Required: " + request.getRequiredVolume() + ", Available: " + totalAvailable)
                    .deductedVolume(0)
                    .build(), "Failed");
        }

        // 3. Thực hiện trừ dần
        double remainingNeeded = request.getRequiredVolume();
        double totalDeducted = 0;

        for (ReagentLot lot : availableLots) {
            if (remainingNeeded <= 0) break;

            double deductAmount = Math.min(lot.getCurrentQuantity(), remainingNeeded);

            // Cập nhật Lot
            lot.setCurrentQuantity(lot.getCurrentQuantity() - deductAmount);
            reagentLotRepository.save(lot);

            // Ghi Log Usage
            ReagentUsageHistory usage = new ReagentUsageHistory();
            usage.setReagentLot(lot);
            usage.setQuantityUsed(deductAmount);
            usage.setAction("ORDER_DEDUCTION");
            usageHistoryRepository.save(usage);

            // Ghi Event Log
            logService.logEvent(WarehouseActionType.REAGENT_USED, lot.getId(), "ReagentLot",
                    "Deducted " + deductAmount + " for Order " + request.getOrderId());

            remainingNeeded -= deductAmount;
            totalDeducted += deductAmount;
        }

        return ApiResponse.success(ReagentDeductionResponse.builder()
                .deductionSuccessful(true)
                .message("Deducted successfully")
                .deductedVolume(totalDeducted)
                .build(), "Success");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkReagentAvailability(String reagentName, Double requiredVolume) {
        log.info("Checking availability for reagent: {} with required volume: {}", reagentName, requiredVolume);

        // Find available lots using FEFO
        List<ReagentLot> availableLots = reagentLotRepository.findAvailableLotsByName(
                reagentName, LocalDate.now());

        if (availableLots.isEmpty()) {
            log.warn("No available lots found for reagent: {}", reagentName);
            return false;
        }

        validateRequiredVolume(reagentName, requiredVolume);

        // Calculate total available quantity
        double totalAvailable = availableLots.stream()
                .mapToDouble(ReagentLot::getCurrentQuantity)
                .sum();

        boolean isAvailable = totalAvailable >= requiredVolume;

        log.info("Availability check result for {}: {} - Required: {}, Available: {}",
                reagentName, isAvailable, requiredVolume, totalAvailable);

        return isAvailable;
    }

    private void validateRequiredVolume(String reagentName, Double requiredVolume) {
        if (!USAGE_LIMITS.containsKey(reagentName)) {
            throw new BadRequestException("Unknown reagent: " + reagentName);
        }

        ReagentUsageLimit limit = USAGE_LIMITS.get(reagentName);

        if (requiredVolume < limit.getMin() || requiredVolume > limit.getMax()) {
            throw new BadRequestException(
                    "Required volume for reagent '" + reagentName + "' must be between " +
                            limit.getMin() + " and " + limit.getMax()
            );
        }
    }
    @Override
    @Transactional
    public ApiResponse<Boolean> deductReagentForInstallation(ReagentInstallationDeductionRequest request) {
        log.info("Processing installation deduction for Lot: {}, Quantity: {}", request.getLotNumber(), request.getQuantity());

        // 1. Tìm Lô hàng cụ thể
        // VÌ REPO TRẢ VỀ ĐỐI TƯỢNG (CÓ THỂ NULL), TA KHÔNG DÙNG .orElseThrow() TRỰC TIẾP ĐƯỢC
        ReagentLot lot = reagentLotRepository.findByLotNumber(request.getLotNumber());

        // Kiểm tra thủ công
        if (lot == null) {
            throw new NotFoundException("Reagent Lot not found: " + request.getLotNumber());
        }

        // 2. Kiểm tra số lượng tồn
        if (lot.getCurrentQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock for Lot " + request.getLotNumber() +
                    ". Available: " + lot.getCurrentQuantity() + ", Requested: " + request.getQuantity());
        }

        // 3. Trừ kho
        double oldQuantity = lot.getCurrentQuantity();
        lot.setCurrentQuantity(oldQuantity - request.getQuantity());
        reagentLotRepository.save(lot);

        ReagentUsageHistory usage = new ReagentUsageHistory();
        usage.setReagentLot(lot);
        usage.setCreatedByUserId(SecurityUtils.getCurrentUserId());
        usage.setQuantityUsed(request.getQuantity());
        usage.setAction("INSTALL_ON_INSTRUMENT"); // Action riêng cho việc cài đặt
        usage.setCreatedAt(LocalDateTime.now()); // Giả sử entity có field này
        usageHistoryRepository.save(usage);

        // 5. Ghi Log sự kiện kho
        logService.logEvent(WarehouseActionType.REAGENT_USED, lot.getId(), "ReagentLot",
                "Deducted " + request.getQuantity() + " for Installation on " + request.getInstrumentId());

        return ApiResponse.success(true, "Deduction successful");
    }

}