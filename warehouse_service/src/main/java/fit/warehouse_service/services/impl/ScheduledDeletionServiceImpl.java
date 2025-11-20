/*
 * @ {#} ScheduledDeletionServiceImpl.java   1.0     30/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import fit.warehouse_service.entities.Instrument;
import fit.warehouse_service.entities.ScheduledDeletion;
import fit.warehouse_service.enums.WarehouseActionType;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.repositories.InstrumentRepository;
import fit.warehouse_service.repositories.ScheduledDeletionRepository;
import fit.warehouse_service.services.ScheduledDeletionService;
import fit.warehouse_service.services.WarehouseEventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Service implementation for managing scheduled deletions of instruments.
 * @author: Tran Hien Vinh
 * @date:   30/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledDeletionServiceImpl implements ScheduledDeletionService {
    private final ScheduledDeletionRepository scheduledDeletionRepository;

    private final InstrumentRepository instrumentRepository;

    private final WarehouseEventLogService logService;

    private static final int DELETION_DELAY_MONTHS = 3;

    @Override
    @Transactional
    public ScheduledDeletion scheduleInstrumentDeletion(String instrumentId, String reason) {
        // Kiểm tra thiết bị y tế tồn tại
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with ID: " + instrumentId));

        // Hủy bỏ bất kỳ lịch xóa nào đã tồn tại
        cancelScheduledDeletion(instrumentId);

        // Tính toán thời gian xóa dự kiến
        LocalDateTime deactivationTime = LocalDateTime.now();
        LocalDateTime scheduledDeletionTime = deactivationTime.plusMonths(DELETION_DELAY_MONTHS);

        // Tạo lịch xóa mới
        ScheduledDeletion scheduledDeletion = ScheduledDeletion.builder()
                .instrumentId(instrumentId)
                .deactivationTime(deactivationTime)
                .scheduledDeletionTime(scheduledDeletionTime)
                .reason(reason)
                .cancelled(false)
                .build();

        // Lưu lịch xóa vào cơ sở dữ liệu
        ScheduledDeletion saved = scheduledDeletionRepository.save(scheduledDeletion);

        // Ghi log sự kiện lên lịch xóa
        String logDetails = logService.createScheduledDeletionDetails(instrument, scheduledDeletionTime, reason);
        logService.logEvent(
                WarehouseActionType.INSTRUMENT_DELETION_SCHEDULED,
                instrumentId,
                "Instrument",
                logDetails
        );

        log.info("Scheduled deletion for instrument {} at {}", instrumentId, scheduledDeletionTime);

        return saved;
    }

    @Override
    @Transactional
    public void cancelScheduledDeletion(String instrumentId) {
        // Tìm lịch xóa hiện tại chưa bị hủy bỏ
        Optional<ScheduledDeletion> existingSchedule = scheduledDeletionRepository
                .findByInstrumentIdAndCancelledFalse(instrumentId);

        // Nếu có, hủy bỏ nó
        if (existingSchedule.isPresent()) {
            ScheduledDeletion scheduledDeletion = existingSchedule.get();
            scheduledDeletion.setCancelled(true);
            scheduledDeletion.setCancellationTime(LocalDateTime.now());

            scheduledDeletionRepository.save(scheduledDeletion);

            // Ghi log sự kiện hủy bỏ lịch xóa
            String logDetails = logService.createDeletionCancellationDetails(instrumentId, scheduledDeletion.getScheduledDeletionTime());
            logService.logEvent(
                    WarehouseActionType.INSTRUMENT_DELETION_CANCELLED,
                    instrumentId,
                    "Instrument",
                    logDetails
            );

            log.info("Cancelled scheduled deletion for instrument {}", instrumentId);
        }
    }

    @Override
    @Transactional
    public void executePendingDeletions() {
        LocalDateTime currentTime = LocalDateTime.now();

        // Tìm tất cả các lịch xóa đã đến thời gian thực hiện và chưa bị hủy bỏ
        List<ScheduledDeletion> pendingDeletions = scheduledDeletionRepository
                .findDeletionsReadyForExecution(currentTime);

        log.info("Found {} instruments scheduled for deletion", pendingDeletions.size());

        // Thực hiện xóa từng thiết bị y tế
        for (ScheduledDeletion scheduledDeletion : pendingDeletions) {
            try {
                // Xóa thiết bị y tế vĩnh viễn
                deleteInstrumentPermanently(scheduledDeletion.getInstrumentId());

                // Cập nhật lịch xóa là đã hoàn thành (bằng cách đánh dấu là hủy để không lặp lại)
                scheduledDeletion.setCancelled(true);
                scheduledDeletion.setCancellationTime(currentTime);
                scheduledDeletionRepository.save(scheduledDeletion);

            } catch (Exception e) {
                log.error("Failed to delete instrument {}: {}",
                        scheduledDeletion.getInstrumentId(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void deleteInstrumentPermanently(String instrumentId) {
        // Tìm thiết bị y tế theo ID
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found with ID: " + instrumentId));

        // Ghi log sự kiện xóa thiết bị y tế
        String logDetails = logService.createInstrumentDeletionDetails(instrument);
        logService.logEvent(
                WarehouseActionType.INSTRUMENT_DELETED,
                instrumentId,
                "Instrument",
                logDetails
        );

        // Xóa thiết bị y tế khỏi cơ sở dữ liệu
        instrumentRepository.delete(instrument);

        log.info("Permanently deleted instrument {} ({})", instrumentId, instrument.getName());
    }
}
