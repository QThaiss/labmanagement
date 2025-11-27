/*
 * @ (#) TestResultSyncService.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.instrument_service.configs.RabbitMQConfig;
import fit.instrument_service.events.TestResultPublishedEvent;
import fit.instrument_service.entities.RawTestResult;
import fit.instrument_service.events.TestResultSyncRequestEvent;
import fit.instrument_service.repositories.RawTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultSyncService {

    private final RawTestResultRepository rawTestResultRepository;
    private final RabbitTemplate rabbitTemplate;

    public void processSyncRequest(TestResultSyncRequestEvent request) {
        log.info("Processing sync request {} for barcodes: {}", request.getRequestId(), request.getBarcodes());

        List<String> barcodes = request.getBarcodes();
        if (barcodes == null || barcodes.isEmpty()) return;

        List<RawTestResult> results = rawTestResultRepository.findByBarcodeIn(barcodes);

        if (results.isEmpty()) {
            log.warn("No raw results found for requested barcodes");
            return;
        }

        for (RawTestResult raw : results) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> resultDataMap = (Map<String, String>) raw.getRawResultData();

                TestResultPublishedEvent event = TestResultPublishedEvent.builder()
                        .instrumentId(raw.getInstrumentId())
                        .testOrderId(raw.getTestOrderId())
                        .barcode(raw.getBarcode())
                        .hl7Message(raw.getHl7Message())
                        .rawResultData(resultDataMap) // Đã ép kiểu
                        .publishedAt(LocalDateTime.now())
                        .build();

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.INSTRUMENT_EXCHANGE,
                        "instrument.test_result",
                        event
                );
                log.info("Re-published result for barcode: {}", raw.getBarcode());

            } catch (Exception e) {
                log.error("Failed to re-publish result for barcode: {}", raw.getBarcode(), e);
            }
        }
    }
}
