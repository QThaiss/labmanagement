/*
 * @ (#) TestResultSyncScheduler.java    1.0    25/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.schedulers;/*
 * @description:
 * @author: Bao Thong
 * @date: 25/11/2025
 * @version: 1.0
 */

import fit.test_order_service.dtos.event.TestResultSyncRequestEvent;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.repositories.TestOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultSyncScheduler {

    private final TestOrderRepository testOrderRepository;
    private final RabbitTemplate rabbitTemplate;

    // Chạy mỗi 5 phút (hoặc config theo cron)
    @Scheduled(fixedRate = 300000)
    public void scanAndRequestSync() {
        log.info("Scanning for stuck test orders...");

        // Tìm các order tạo cách đây hơn 10 phút mà chưa hoàn thành
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(10);
        List<TestOrder> stuckOrders = testOrderRepository.findStuckOrders(timeout);

        if (stuckOrders.isEmpty()) {
            return;
        }

        List<String> barcodes = stuckOrders.stream()
                .map(TestOrder::getBarcode) // Giả sử TestOrder có field barcode
                .collect(Collectors.toList());

        log.info("Found {} stuck orders. Requesting sync for barcodes: {}", stuckOrders.size(), barcodes);

        // Tạo event yêu cầu sync
        TestResultSyncRequestEvent requestEvent = TestResultSyncRequestEvent.builder()
                .requestId(UUID.randomUUID().toString())
                .requestedBy("TEST_ORDER_SERVICE")
                .barcodes(barcodes)
                .build();

        // Gửi sang Instrument Service
        // Lưu ý: Cần config Exchange/RoutingKey tương ứng trong RabbitMQConfig của TestOrderService
        rabbitTemplate.convertAndSend(
                "instrument.exchange", // Tên Exchange của Instrument Service
                "instrument.sync.request",
                requestEvent
        );
    }
}
