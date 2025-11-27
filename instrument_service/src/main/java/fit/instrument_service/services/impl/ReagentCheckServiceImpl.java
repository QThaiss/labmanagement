/*
 * @ {#} ReagentCheckServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.AuditAction;
import fit.instrument_service.enums.ReagentStatus;
import fit.instrument_service.exceptions.BadRequestException;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.repositories.InstrumentReagentRepository;
import fit.instrument_service.services.AuditLogService;
import fit.instrument_service.services.ReagentCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/*
 * @description: Service kiểm tra hóa chất của thiết bị
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentCheckServiceImpl implements ReagentCheckService {
    private final InstrumentReagentRepository instrumentReagentRepository;
    private final AuditLogService auditLogService;

    private static final int MINIMUM_REAGENT_QUANTITY = 10;

    @Override
    public boolean areReagentsSufficient(String instrumentId) {
        log.info("Checking reagent levels for instrument: {}", instrumentId);

        // Lấy danh sách hóa chất đang sử dụng cho thiết bị
        List<InstrumentReagent> reagentsInUse = instrumentReagentRepository
                .findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE);

        // Nếu không có hóa chất nào đang sử dụng thì coi như không đủ
        if (reagentsInUse.isEmpty()) {
            log.warn("No reagents in use for instrument: {}", instrumentId);
            return false;
        }

        LocalDate today = LocalDate.now();

        // Kiểm tra từng hóa chất
        for (InstrumentReagent reagent : reagentsInUse) {
            // Kiểm tra số lượng
            if (reagent.getQuantity() == null || reagent.getQuantity() < MINIMUM_REAGENT_QUANTITY) {
                log.warn("Insufficient quantity for reagent: {} (current: {})",
                        reagent.getReagentName(), reagent.getQuantity());
                return false;
            }

            // Kiểm tra hạn sử dụng
            if (reagent.getExpirationDate() == null || reagent.getExpirationDate().isBefore(today)) {
                log.warn("Expired reagent: {} (expiration date: {})",
                        reagent.getReagentName(), reagent.getExpirationDate());
                return false;
            }
        }

        log.info("Reagent levels are sufficient for instrument: {}", instrumentId);
        return true;
    }


    @Override
    @Transactional
    public void uninstallReagent(String instrumentId, String instrumentReagentId, String reason) { //
        log.info("Attempting to uninstall reagent lot {} from instrument {}", instrumentReagentId, instrumentId);

        // 1. Tìm bản ghi InstrumentReagent
        // Giả định các Exception (NotFoundException, BadRequestException) đã được import
        InstrumentReagent instrumentReagent = instrumentReagentRepository.findById(instrumentReagentId)
                .orElseThrow(() -> new NotFoundException("Instrument Reagent record not found with ID: " + instrumentReagentId));

        // 2. Kiểm tra tính hợp lệ (bản ghi phải thuộc về máy này)
        if (!instrumentReagent.getInstrumentId().equals(instrumentId)) {
            log.warn("Instrument Reagent ID {} does not belong to Instrument ID {}", instrumentReagentId, instrumentId);
            throw new BadRequestException("Instrument Reagent ID does not match the specified Instrument ID.");
        }

        // 3. Thực hiện gỡ bỏ (xóa bản ghi)
        instrumentReagentRepository.delete(instrumentReagent);
        log.info("Reagent lot {} successfully uninstalled from instrument {}. Record deleted.", instrumentReagentId, instrumentId);

        // 4. Ghi log hành động
        Map<String, Object> details = Map.of(
                "action", "Uninstall Reagent",
                "instrumentId", instrumentId,
                "reagentLotNumber", instrumentReagent.getLotNumber(),
                "reagentTypeName", instrumentReagent.getReagentName(),
                "reason", reason != null ? reason : "No reason provided",
                "remainingQuantity", instrumentReagent.getQuantity() // Log số lượng còn lại khi gỡ bỏ
        );

        // Sử dụng DELETE_REAGENT cho hành động uninstallation
        auditLogService.logAction(
                AuditAction.DELETE_REAGENT,
                instrumentId,
                "InstrumentReagent",
                details
        );
    }

}
