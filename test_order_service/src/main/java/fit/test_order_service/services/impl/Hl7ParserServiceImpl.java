/*
 * @ {#} Hl7ParserServiceImpl.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import fit.test_order_service.dtos.response.Hl7Metadata;
import fit.test_order_service.dtos.response.ParsedTestResult;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.enums.AbnormalFlag;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.services.Hl7ParserService;
import fit.test_order_service.utils.TestCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * @description: Implement of Hl7ParserService to parse HL7 messages.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Hl7ParserServiceImpl implements Hl7ParserService {
    private final Parser parser;

    private final TestCodeGenerator testCodeGenerator;

    @Override
    public List<ParsedTestResult> parseHl7Message(String hl7Payload) {
        try {
            // Phân tích cú pháp tin nhắn HL7 bằng HAPI
            Message message = parser.parse(hl7Payload);

            // Nếu là tin nhắn ORU_R01, xử lý tiếp
            if (message instanceof ORU_R01) {
                return parseOruR01Message((ORU_R01) message);
            } else {
                throw new BadRequestException("Unsupported message type: " + message.getClass().getSimpleName());
            }

        } catch (HL7Exception e) {
            log.error("Failed to parse HL7 message: {}", e.getMessage());
            throw new BadRequestException("Invalid HL7 format: " + e.getMessage());
        }
    }

    @Override
    public Hl7Metadata extractMetadata(String hl7Payload) {
        try {
            Message message = parser.parse(hl7Payload);
            if (message instanceof ORU_R01 oru) {
                String messageId = oru.getMSH().getMessageControlID().getValue();
                String app = oru.getMSH().getSendingApplication().getNamespaceID().getValue();
                String facility = oru.getMSH().getSendingFacility().getNamespaceID().getValue();
                return Hl7Metadata.builder()
                        .messageId(messageId)
                        .sendingApplication(app)
                        .sendingFacility(facility)
                        .build();
            }
        } catch (Exception e) {
            log.warn("Failed to extract metadata: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String buildHl7OrderMessage(TestOrder testOrder, List<TestResult> testResults) {
        StringBuilder hl7 = new StringBuilder();

        // --- MSH Segment ---
        hl7.append("MSH|^~\\&|TEST-ORDER|SYSTEM|")
                .append("INSTRUMENT")
                .append("|LAB|")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .append("||OML^O21|MSG")
                .append(UUID.randomUUID().toString().substring(0, 8))
                .append("|P|2.5\r");

        // --- PID Segment ---
        hl7.append("PID|1||")
                .append(testOrder.getMedicalRecordCode()).append("||")
                .append(testOrder.getFullName()).append("||")
                .append(testOrder.getDateOfBirth().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append("|")
                .append(testOrder.getGender().name().charAt(0)) // ví dụ: M / F
                .append("|||")
                .append(testOrder.getAddress() != null ? testOrder.getAddress() : "UNKNOWN")
                .append("||")
                .append(testOrder.getPhone())
                .append("\r");

        // OBR Segment (Thông tin của TestOrder)
        hl7.append("OBR|1||")
                .append(testOrder.getOrderCode()) // Mã order
                .append("|PANEL^Complete Blood Count") // Có thể tuỳ chỉnh theo nhóm test
                .append("|||")
                .append(testOrder.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .append("\r");

        // OBX Segments (Thông tin của từng kết quả xét nghiệm dự kiến)
        int index = 1;
        if (testResults != null) {
            for (TestResult result : testResults) {
                hl7.append("OBX|")
                        .append(index++).append("|") // Set ID
                        .append("ST|") // Value Type: String
                        .append(result.getTestCode() != null ? result.getTestCode() : testCodeGenerator.generateTemporaryCode())
                        .append("^")
                        .append(result.getAnalyteName())
                        .append("|1|") // Observation Sub-ID
                        .append("|") // Không có giá trị vì là yêu cầu, không phải kết quả
                        .append(result.getUnit() != null ? result.getUnit() : "")
                        .append("|") // Unit
                        .append(result.getReferenceRange() != null ? result.getReferenceRange() : "")
                        .append("|N|||P") // Normal, Preliminary
                        .append("\r");
            }
        }

        return hl7.toString();
    }

    // Phân tích tin nhắn ORU_R01 để trích xuất kết quả xét nghiệm
    private List<ParsedTestResult> parseOruR01Message(ORU_R01 oruMessage) throws HL7Exception {
        // Khởi tạo danh sách kết quả xét nghiệm đã phân tích
        List<ParsedTestResult> results = new ArrayList<>();

        // Lấy số lượng ORDER_OBSERVATION trong tin nhắn
        int orderObservationReps = oruMessage.getPATIENT_RESULT().getORDER_OBSERVATIONReps();

        // Duyệt qua từng ORDER_OBSERVATION
        for (int i = 0; i < orderObservationReps; i++) {
            // Lấy ORDER_OBSERVATION hiện tại
            ORU_R01_ORDER_OBSERVATION orderObs = oruMessage.getPATIENT_RESULT().getORDER_OBSERVATION(i);

            // Lấy OBR (Order) segment để lấy thông tin test order
            OBR obr = orderObs.getOBR();

            // Lấy Order ID từ OBR segment
            String orderId = obr.getFillerOrderNumber().getEntityIdentifier().getValue();

            // Lấy số lượng OBSERVATION trong ORDER_OBSERVATION
            int observationReps = orderObs.getOBSERVATIONReps();

            // Duyệt qua từng OBSERVATION để trích xuất kết quả xét nghiệm
            for (int j = 0; j < observationReps; j++) {
                // Lấy OBX (Observation/Result) segment
                OBX obx = orderObs.getOBSERVATION(j).getOBX();

                // Phân tích OBX để trích xuất kết quả xét nghiệm
                ParsedTestResult result = parseObservation(obx, orderId);
                if (result != null) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    // Phân tích OBX segment để trích xuất kết quả xét nghiệm
    private ParsedTestResult parseObservation(OBX obx, String orderId) throws HL7Exception {
        try {
            // Trích xuất observation identifier
            CE observationIdentifier = obx.getObservationIdentifier();

            // Trích xuất mã xét nghiệm từ HL7
            String rawTestCode = observationIdentifier.getIdentifier().getValue();

            // Trích xuất tên xét nghiệm
            String analyteName = observationIdentifier.getText().getValue();

            // Chuẩn hóa testCode thông qua TestCatalog
            String testCode = normalizeTestCode(rawTestCode, analyteName);

            // Trích xuất giá trị kết quả
            String valueText = "";
            if (obx.getObservationValue().length > 0) {
                valueText = obx.getObservationValue()[0].getData().toString();
            }

            // Trích xuất đơn vị đo
            String unit = "";
            if (obx.getUnits() != null) {
                unit = obx.getUnits().getIdentifier().getValue();
            }

            // Trích xuất khoảng tham chiếu
            String referenceRange = "";
            if (obx.getReferencesRange() != null) {
                referenceRange = obx.getReferencesRange().getValue();
            }

            // Trích xuất cờ bất thường
            String abnormalFlagStr = "";
            if (obx.getAbnormalFlags().length > 0) {
                abnormalFlagStr = obx.getAbnormalFlags()[0].getValue();
            }

            // Phân tích cờ bất thường
            AbnormalFlag abnormalFlag = parseAbnormalFlag(abnormalFlagStr);

            // Trích xuất thời gian đo lường
            LocalDateTime measuredAt = parseTimestamp(obx.getDateTimeOfTheObservation());

            // Trả về đối tượng ParsedTestResult
            return ParsedTestResult.builder()
                    .orderId(orderId)
                    .testCode(testCode)
                    .analyteName(analyteName != null ? analyteName : testCode)
                    .valueText(valueText)
                    .unit(unit)
                    .referenceRange(referenceRange)
                    .abnormalFlag(abnormalFlag)
                    .measuredAt(measuredAt)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse OBX observation: {}", e.getMessage());
            return null;
        }
    }

    private LocalDateTime parseTimestamp(TS ts) {
        if (ts == null || ts.getTime() == null) {
            return null;
        }
        String value = ts.getTime().getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(value.substring(0, Math.min(value.length(), 14)), formatter);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp {}: {}", value, e.getMessage());
            return null;
        }
    }

    // Phân tích cờ bất thường từ chuỗi ký tự
    private AbnormalFlag parseAbnormalFlag(String flagStr) {
        // Kiểm tra null hoặc chuỗi rỗng
        if (flagStr == null || flagStr.trim().isEmpty()) {
            return null;
        }

        // Chuyển đổi chuỗi ký tự thành enum AbnormalFlag
        switch (flagStr.toUpperCase()) {
            case "H": return AbnormalFlag.H;
            case "L": return AbnormalFlag.L;
            case "N": return AbnormalFlag.N;
            case "A": return AbnormalFlag.A;
            default: return null;
        }
    }

    private String normalizeTestCode(String rawTestCode, String analyteName) {
        if (rawTestCode != null && !rawTestCode.isBlank()) {
            return rawTestCode;
        }
        if (analyteName != null && !analyteName.isBlank()) {
            return testCodeGenerator.generateFromName(analyteName);
        }
        return testCodeGenerator.generateTemporaryCode();
    }

    // Phân tích timestamp từ kiểu dữ liệu TS của HL7
//    private LocalDateTime parseTimestamp(TS timestamp) {
//        // Kiểm tra null
//        if (timestamp == null || timestamp.getTime() == null) {
//            return LocalDateTime.now();
//        }
//
//        try {
//            // Lấy chuỗi thời gian
//            String timeStr = timestamp.getTime().getValue();
//            // HL7 timestamp format: YYYYMMDDHHMMSS
//            if (timeStr.length() >= 8) {
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//
//                // Đảm bảo chuỗi có đủ độ dài
//                while (timeStr.length() < 14) {
//                    timeStr += "0";
//                }
//
//                // Trá về LocalDateTime đã phân tích
//                return LocalDateTime.parse(timeStr, formatter);
//            }
//        } catch (Exception e) {
//            log.warn("Failed to parse timestamp: {}", timestamp.getTime().getValue());
//        }
//
//        return LocalDateTime.now();
//    }
}
