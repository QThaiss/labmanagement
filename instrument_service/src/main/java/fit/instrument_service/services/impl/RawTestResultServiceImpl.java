/*
 * @ (#) RawTestResultServiceImpl.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import fit.instrument_service.configs.RabbitMQConfig;
import fit.instrument_service.dtos.request.DeleteRawResultRequest;
import fit.instrument_service.entities.RawTestResult;
import fit.instrument_service.events.RawResultDeletedEvent;
import fit.instrument_service.exceptions.BadRequestException;
import fit.instrument_service.repositories.RawTestResultRepository;
import fit.instrument_service.services.RawTestResultService;
import fit.instrument_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawTestResultServiceImpl implements RawTestResultService {

    private final RawTestResultRepository rawTestResultRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${instrument.raw-result.retention-days:30}")
    private int retentionDays;

    @Override
    @Transactional
    public void deleteRawResults(DeleteRawResultRequest request) {
        // 1. Audit Trail: Who performed the deletion
        String currentUser = SecurityUtils.getCurrentUserId();
        if (currentUser == null) {
            currentUser = "Unknown User";
        }

        if (request.getRawResultIds() == null || request.getRawResultIds().isEmpty()) {
            throw new BadRequestException("Danh sách ID không được để trống");
        }

        // 2. Tìm kiếm trong DB
        List<RawTestResult> foundResults = rawTestResultRepository.findAllById(request.getRawResultIds());

        // 3. Logic cốt lõi SRS 3.6.1.5: Chỉ xóa data đã backup (isReadyForDeletion == true)
        List<RawTestResult> deletableResults = foundResults.stream()
                .filter(RawTestResult::isReadyForDeletion)
                .collect(Collectors.toList());

        if (deletableResults.isEmpty()) {
            throw new BadRequestException("Không có bản ghi nào đủ điều kiện xóa. (Yêu cầu: Phải được backup sang Monitoring Service trước)");
        }

        List<String> deletedBarcodes = deletableResults.stream()
                .map(RawTestResult::getBarcode)
                .collect(Collectors.toList());

        // 4. Xóa vật lý
        rawTestResultRepository.deleteAll(deletableResults);
        log.info("User {} deleted {} raw results.", currentUser, deletableResults.size());

        // 5. Publish Event (Audit Trail)
        // Tạo payload event
        RawResultDeletedEvent event = RawResultDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .performedBy(currentUser)
                .deletedBarcodes(deletedBarcodes)
                .deletedAt(LocalDateTime.now())
                .details("Manual deletion via Instrument Service")
                .build();

        // Gửi message
        // SỬ DỤNG: RabbitMQConfig.INSTRUMENT_EXCHANGE (có sẵn)
        // ROUTING KEY: Dùng chuỗi String trực tiếp, ví dụ "instrument.event"
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    "instrument.event", // <--- Routing key cứng, khớp với binding bên Monitoring
                    event
            );
        } catch (Exception e) {
            log.error("Lỗi khi gửi event audit log", e);
            // Không throw exception để tránh rollback việc xóa DB
        }
    }

    @Override
    @Transactional
    public void executeAutoDeletion() {
        log.info("Starting auto-deletion background job...");

        // 1. Xác định ngưỡng thời gian (Ví dụ: Xóa data cũ hơn 30 ngày)
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        // 2. Tìm các bản ghi đủ điều kiện (Ready for deletion + Old enough)
        List<RawTestResult> oldResults = rawTestResultRepository.findDeletableOldResults(threshold);

        if (oldResults.isEmpty()) {
            log.info("No old raw results found to delete.");
            return;
        }

        List<String> deletedBarcodes = oldResults.stream()
                .map(RawTestResult::getBarcode)
                .collect(Collectors.toList());

        // 3. Thực hiện xóa
        rawTestResultRepository.deleteAll(oldResults);
        log.info("Auto-deleted {} old raw results created before {}", oldResults.size(), threshold);

        // 4. Publish Event Audit Trail (SRS 3.6.1.6 requires audit log)
        RawResultDeletedEvent event = RawResultDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .performedBy("SYSTEM_SCHEDULER") // Người thực hiện là hệ thống
                .deletedBarcodes(deletedBarcodes)
                .deletedAt(LocalDateTime.now())
                .details("Auto-deletion of " + oldResults.size() + " records older than " + retentionDays + " days.")
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE,
                    "instrument.event", // Routing key thống nhất với Monitoring Service
                    event
            );
        } catch (Exception e) {
            log.error("Failed to publish auto-deletion audit event", e);
        }
    }
}
