/*
 * @ (#) TestResultSyncListener.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.listeners;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import fit.test_order_service.services.impl.RabbitMQConfig;
import fit.test_order_service.dtos.event.TestResultPublishedEvent;
import fit.test_order_service.dtos.request.Hl7MessageRequest;
import fit.test_order_service.dtos.response.Hl7ProcessResponse;
import fit.test_order_service.services.Hl7ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TestResultSyncListener {

    private final Hl7ProcessingService hl7ProcessingService;

    /**
     * Lắng nghe sự kiện Test Result được publish từ Instrument Service.
     * Thực hiện yêu cầu SRS 3.5.2.2: New Test Results Sync-up.
     * Cơ chế này đảm bảo tính toàn vẹn dữ liệu (Data Integrity) và Fault Tolerance.
     *
     * @param event Sự kiện chứa thông tin kết quả xét nghiệm và bản tin HL7.
     */
    @RabbitListener(queues = RabbitMQConfig.TEST_RESULT_QUEUE)
    public void handleTestResultSync(TestResultPublishedEvent event) {
        log.info("Received Test Result Sync Event for Barcode: {} from Instrument: {}",
                event.getBarcode(), event.getInstrumentId());

        try {
            // 1. Trích xuất HL7 Message từ sự kiện
            String hl7Payload = event.getHl7Message();

            if (hl7Payload == null || hl7Payload.isEmpty()) {
                log.warn("Received empty HL7 payload for barcode: {}", event.getBarcode());
                return;
            }

            // 2. Tạo request để tái sử dụng logic xử lý HL7 có sẵn
            Hl7MessageRequest request = new Hl7MessageRequest();
            request.setHl7Payload(hl7Payload);

            // 3. Gọi Hl7ProcessingService để xử lý (Parse -> Validate -> Save -> Flagging)
            // Lưu ý: Service này đã handle transaction và lưu vào các bảng Result, RawMessage, Audit.
            Hl7ProcessResponse response = hl7ProcessingService.processHl7Message(request);

            if ("SUCCESS".equals(response.getStatus())) {
                log.info("Successfully synced results for Barcode: {}. Result IDs: {}",
                        event.getBarcode(), response.getResultIds());

            } else if ("DUPLICATE".equals(response.getStatus())) {
                // Xử lý êm đẹp cho trường hợp trùng lặp
                log.info("Sync skipped for Barcode: {} - Reason: Duplicate Message ID {}",
                        event.getBarcode(), response.getMessageId());

            } else {
                // Chỉ log error khi thực sự lỗi (FAILED)
                log.error("Failed to process test results for Barcode: {}. Error: {}",
                        event.getBarcode(), response.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Critical error during Test Result Sync for Barcode: {}", event.getBarcode(), e);
            // Ném ngoại lệ để RabbitMQ có thể requeue message (hoặc đẩy vào DLQ tuỳ config)
            throw e;
        }
    }
}
