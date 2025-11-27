/*
 * @ {#} Hl7OrderSenderServiceImpl.java   1.0     11/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import fit.test_order_service.dtos.request.Hl7MessageRequest;
import fit.test_order_service.dtos.request.InstrumentOrderRequest;
import fit.test_order_service.dtos.response.Hl7ProcessResponse;
import fit.test_order_service.dtos.response.TestOrderDetailResponse;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.mappers.TestOrderMapper;
import fit.test_order_service.repositories.TestOrderRepository;
import fit.test_order_service.services.Hl7OrderSenderService;
import fit.test_order_service.services.Hl7ParserService;
import fit.test_order_service.services.Hl7ProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/*
 * @description: Implementation of HL7 order sender service
 * @author: Tran Hien Vinh
 * @date:   11/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Hl7OrderSenderServiceImpl implements Hl7OrderSenderService {
    private final Hl7ParserService hl7ParserService;

    private final Hl7ProcessingService hl7ProcessService;

    private final TestOrderRepository testOrderRepository;

    private final TestOrderMapper testOrderMapper;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Hl7ProcessResponse sendOrderAndProcessResult(String testOrderId) {
        // Lấy order và items
        TestOrder order = testOrderRepository.findById(testOrderId)
                .orElseThrow(() -> new NotFoundException("Test order not found: " + testOrderId));
        List<TestResult> results = order.getResults();

        // Gửi order đi và nhận về HL7 response
        String hl7Response = sendOrderToInstrument(order, results);

        // Tạo request object cho processHl7Message
        Hl7MessageRequest hl7Request = new Hl7MessageRequest();
        hl7Request.setHl7Payload(hl7Response);

        // Gọi xử lý message HL7 raw nhận được
        return hl7ProcessService.processHl7Message(hl7Request);
    }

    @Override
    public String sendOrderToInstrument(TestOrder order, List<TestResult> results) {
        // Xây dựng HL7 từ order và items
        String payload = hl7ParserService.buildHl7OrderMessage(order, results);
        log.info("[HL7] Sending OML^O21 for order {}...", order.getOrderCode());
        log.debug("HL7 request payload:\n{}", payload);

        // Nếu chưa có kết nối, mock HL7 response
        if (isMockMode()) {
            log.info("[MOCK] Simulating analyzer response for order {}", order.getOrderCode());
            return buildMockHl7Response(order, results);
        }

//        // Nếu có kết nối thật
//        String framed = (char) 0x0B + payload + (char) 0x1C + (char) 0x0D;
//        String response = hl7SocketClient.send(framed);
//        log.info("[HL7] Received raw response:\n{}", response);
//        return response;
        return null;
    }

    @Override
    @Transactional
    public String requestAnalysis(String testOrderId) {
        // 1. Lấy order và items
        TestOrder order = testOrderRepository.findByOrderIdAndDeletedFalse(testOrderId)
                .orElseThrow(() -> new NotFoundException("Test order not found: " + testOrderId));

        // 2. Kiểm tra trạng thái
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is already processing or completed. Current status: " + order.getStatus());
        }

        // 3. Map entity sang DTO chi tiết
        TestOrderDetailResponse orderDetails = testOrderMapper.toDetailResponse(order);

        // 4. Tạo đối tượng message cho hàng đợi
        InstrumentOrderRequest requestDto = new InstrumentOrderRequest(orderDetails);

        // 5. Gửi message đến RabbitMQ
        try {
            log.info("Publishing order request {} to exchange '{}' with routing key '{}'",
                    order.getOrderId(), RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    requestDto
            );

            // 6. Cập nhật trạng thái order
            order.setStatus(OrderStatus.IN_PROGRESS); // Chuyển trạng thái sang "Đang xử lý"
            testOrderRepository.save(order);

            log.info("Order {} status updated to PROCESSING.", order.getOrderId());

            return "Order " + order.getOrderCode() + " successfully submitted for analysis.";

        } catch (Exception e) {
            log.error("Failed to send order request message for order ID {}: {}", testOrderId, e.getMessage(), e);
            // Nếu gửi message thất bại, transaction sẽ rollback và status không bị thay đổi
            throw new RuntimeException("Failed to queue order analysis request. Please try again.", e);
        }
    }

    // Kiểm tra chế độ mock (dev)
    private boolean isMockMode() {
        return true; // dùng mock mặc định cho dev
    }


    /**
     * Giả lập HL7 ORU^R01 (phản hồi kết quả từ máy)
     */
    private String buildMockHl7Response(TestOrder order, List<TestResult> results) {
        // Xây dựng tin nhắn HL7 ORU^R01
        StringBuilder hl7 = new StringBuilder();
        Random random = new Random();

        // MSH Segment
        hl7.append("MSH|^~\\&|INSTRUMENT|LAB|TEST-ORDER|SYSTEM|")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .append("||ORU^R01|MSG-MOCK-").append(UUID.randomUUID().toString().substring(0, 8))
                .append("|P|2.5\r");

        // PID Segment
        hl7.append("PID|1||")
                .append(order.getMedicalRecordId()).append("||")
                .append(order.getFullName()).append("||")
                .append(order.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append("|")
                .append(order.getGender().name().charAt(0)).append("|||")
                .append(order.getAddress() != null ? order.getAddress() : "UNKNOWN")
                .append("||").append(order.getPhone()).append("\r");

        // OBR Segment
        hl7.append("OBR|1||")
                .append(order.getOrderId()).append("|PANEL^Complete Blood Count|||")
                .append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .append("\r");

        List<TestResult> payloadResults = (results == null || results.isEmpty())
                ? buildDefaultResults(order)
                : results;

        int index = 1;
        for (TestResult result : payloadResults) {
            String code = result.getTestCode() != null ? result.getTestCode().toUpperCase() : "GEN" + index;
            String testName = result.getAnalyteName();

            Map<String, String[]> defaults = Map.of(
                    "GLU", new String[]{"92", "mg/dL", "70-100"},
                    "HGB", new String[]{"14.2", "g/dL", "13.5-17.5"},
                    "WBC", new String[]{"6.8", "10^3/uL", "4.5-11.0"},
                    "PLT", new String[]{"265", "10^3/uL", "150-400"},
                    "RBC", new String[]{"4.9", "10^6/uL", "4.2-5.9"}
            );

            String[] defaultValues = defaults.getOrDefault(code, new String[]{
                    String.format("%.1f", 10 + random.nextDouble() * 5),
                    result.getUnit() != null ? result.getUnit() : "mg/dL",
                    result.getReferenceRange() != null ? result.getReferenceRange() : ""
            });

            String value = defaultValues[0];
            String unit = defaultValues[1];
            String refRange = defaultValues[2];
            String flag = "N";

            double chance = random.nextDouble();
            if (chance < 0.1) flag = "H";
            else if (chance > 0.9) flag = "L";

            hl7.append(String.format(
                    "OBX|%d|NM|%s^%s|%d|%s|%s|%s|%s|||F\r",
                    index++, code, testName, index - 1, value, unit, refRange, flag
            ));
        }

        return hl7.toString();
    }

    private List<TestResult> buildDefaultResults(TestOrder order) {
        return List.of(
                TestResult.builder().orderId(order.getOrderId()).testCode("GLU").analyteName("Glucose")
                        .unit("mg/dL").referenceRange("70-100").valueText("92").build(),
                TestResult.builder().orderId(order.getOrderId()).testCode("HGB").analyteName("Hemoglobin")
                        .unit("g/dL").referenceRange("13.5-17.5").valueText("14.2").build(),
                TestResult.builder().orderId(order.getOrderId()).testCode("WBC").analyteName("White Blood Cell")
                        .unit("10^3/uL").referenceRange("4.5-11.0").valueText("6.8").build()
        );
    }
}
