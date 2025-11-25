/*
 * @ (#) TestResultBackupServiceImpl.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.event.TestResultPublishedEvent;
import fit.monitoring_service.entities.RawTestResultBackup;
import fit.monitoring_service.repositories.RawTestResultBackupRepository;
import fit.monitoring_service.services.TestResultBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultBackupServiceImpl implements TestResultBackupService {

    private final RawTestResultBackupRepository rawTestResultBackupRepository;

    @Override
    @Transactional
    public void backupTestResult(TestResultPublishedEvent event) {
        log.info("Processing backup for Test Result from Instrument: {}", event.getInstrumentId());

        try {
            RawTestResultBackup backup = new RawTestResultBackup();

            // Mapping dữ liệu từ Event sang Entity
            backup.setInstrumentId(event.getInstrumentId());
            backup.setTestOrderId(event.getTestOrderId()); // Có thể null nếu là auto-create chưa match
            backup.setRawHl7Message(event.getHl7Message());

            // Ghi nhận thời điểm nhận được tin nhắn (Captured At) [cite: 461]
            backup.setCapturedAt(LocalDateTime.now());

            // Set trạng thái mặc định cho việc Sync-up sau này [cite: 473]
            backup.setSyncStatus("PENDING");

            rawTestResultBackupRepository.save(backup);

            log.info("Successfully backed up raw test result for Barcode: {}", event.getBarcode());
        } catch (Exception e) {
            log.error("Failed to backup test result: {}", e.getMessage(), e);
            // Có thể cân nhắc throw exception để RabbitMQ requeue nếu cần cơ chế retry
        }
    }
}
