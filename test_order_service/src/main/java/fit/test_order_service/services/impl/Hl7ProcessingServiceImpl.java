/*
 * @ {#} Hl7ProcessingServiceImpl.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.parser.Parser;
import fit.test_order_service.dtos.request.Hl7MessageRequest;
import fit.test_order_service.dtos.response.Hl7Metadata;
import fit.test_order_service.dtos.response.Hl7ProcessResponse;
import fit.test_order_service.dtos.response.Hl7ValidationResult;
import fit.test_order_service.dtos.response.ParsedTestResult;
import fit.test_order_service.entities.*;
import fit.test_order_service.enums.EntrySource;
import fit.test_order_service.enums.IngestStatus;
import fit.test_order_service.enums.ItemStatus;
import fit.test_order_service.exceptions.AlreadyExistsException;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.repositories.*;
import fit.test_order_service.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * @description: Implementation of Hl7ProcessingService to handle HL7 message processing.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class Hl7ProcessingServiceImpl implements Hl7ProcessingService {
    private final Hl7RawMessageRepository rawMessageRepository;

    private final TestResultRepository testResultRepository;

    private final Hl7QuarantineRepository quarantineRepository;

    private final ResultIngestAuditRepository ingestAuditRepository;

    private final FlaggingService flaggingService;

    private final Hl7ParserService hl7ParserService;

    private final TestOrderRepository testOrderRepository;

    private final Hl7Validator hl7Validator;

    private final Parser parser;

    private final TestOrderStatusService testOrderStatusService;

    @Override
    public Hl7ProcessResponse processHl7Message(Hl7MessageRequest request) {
        log.debug("Received HL7 payload:\n{}", request.getHl7Payload());
        // Trích xuất metadata từ payload HL7
        Hl7Metadata metadata = hl7ParserService.extractMetadata(request.getHl7Payload());

        String messageId = metadata.getMessageId();
        String sendingApp = metadata.getSendingApplication();
        String facility = metadata.getSendingFacility();
        String fullSource = (sendingApp != null ? sendingApp : "UNKNOWN")
                + (facility != null ? "-" + facility : "");
        String enteredBy = truncate(sendingApp != null ? sendingApp : fullSource, 36);


        // Kiểm tra trùng lặp message ID
        if (rawMessageRepository.existsByMessageId(messageId)) {
            throw new AlreadyExistsException("HL7 message with ID " + messageId + " already processed");
        }

        // Lưu tin nhắn HL7 thô
        Hl7RawMessage rawMessage = saveRawMessage(request, messageId, fullSource);

        // Tạo bản ghi audit ingest
        ResultIngestAudit ingestAudit = createIngestAudit(messageId, rawMessage.getRawId());

        try {
            // Parse HL7 message
            List<ParsedTestResult> parsedResults = hl7ParserService.parseHl7Message(request.getHl7Payload());

            // Kiểm tra kết quả phân tích có rỗng không
            if (parsedResults.isEmpty()) {
                return handleParsingError(ingestAudit, "No test results found in HL7 message", null);
            }

            // Lấy orderId từ kết quả phân tích đầu tiên
            String orderId = parsedResults.get(0).getOrderId();

            // Thêm validation HL7 structure
            Message message = parser.parse(request.getHl7Payload());
            if (message instanceof ORU_R01 oru) {
                Hl7ValidationResult validationResult = hl7Validator.validateHl7Structure(oru, orderId);
                if (!validationResult.isValid()) {
                    String errorMsg = String.format("HL7 Validation Failed at %s: %s",
                            validationResult.getFieldPath(), validationResult.getErrorMessage());
                    return handleParsingError(ingestAudit, errorMsg, rawMessage);
                }
            }

            // Tìm TestOrder tương ứng với orderId
            TestOrder order = testOrderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return handleParsingError(ingestAudit, "Order not found: " + orderId, rawMessage);
            }

            // Xử lý từng kết quả phân tích
            List<String> resultIds = new ArrayList<>();
            for (ParsedTestResult parsed : parsedResults) {
                // Kiểm tra result status để quyết định persist hay tạm lưu
                if (!shouldPersistResult(parsed, message)) {
                    log.info("Preliminary result for {}, storing temporarily", parsed.getAnalyteName());
                    // Có thể lưu vào bảng tạm hoặc đánh dấu khác
                    continue;
                }

                TestResult result = testResultRepository
                        .findByOrderIdAndAnalyteNameIgnoreCase(orderId, parsed.getAnalyteName())
                        .stream()
                        .findFirst()
                        .map(existing -> updateExistingResult(existing, parsed, messageId, enteredBy))
                        .orElseGet(() -> createTestResult(parsed, messageId, enteredBy, order));

                // Tạo và lưu TestResult
                TestResult saved = testResultRepository.save(result);

                // Thu thập resultId
                resultIds.add(saved.getResultId());

                // Áp dụng quy tắc đánh dấu (flagging rules)
                flaggingService.applyFlaggingRules(saved);
            }

            // Cập nhật trạng thái TestOrder nếu cần
            testOrderStatusService.updateOrderStatusIfNeeded(orderId);

            // Cập nhật audit ingest thành công
            updateIngestAuditSuccess(ingestAudit);

            // Trả về phản hồi thành công
            return Hl7ProcessResponse.builder()
                    .messageId(messageId)
                    .status("SUCCESS")
                    .rawId(rawMessage.getRawId())
                    .resultIds(resultIds)
                    .processedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();

        } catch (Exception e) {
            log.error("Error processing HL7 message {}: {}", messageId, e.getMessage(), e);
            handleParsingError(ingestAudit, e.getMessage(), rawMessage);
            throw new BadRequestException("Failed to parse HL7 message: " + e.getMessage());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private boolean shouldPersistResult(ParsedTestResult parsed, Message message) throws HL7Exception {
        if (message instanceof ORU_R01 oru) {
            // Tìm OBX tương ứng và kiểm tra OBX-11
            int orderObservationReps = oru.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
            for (int i = 0; i < orderObservationReps; i++) {
                ORU_R01_ORDER_OBSERVATION orderObs = oru.getPATIENT_RESULT().getORDER_OBSERVATION(i);
                int observationReps = orderObs.getOBSERVATIONReps();
                for (int j = 0; j < observationReps; j++) {
                    OBX obx = orderObs.getOBSERVATION(j).getOBX();
                    String resultStatus = obx.getObservationResultStatus().getValue();

                    boolean isFinal = hl7Validator.isFinalResult(resultStatus);
                    if (isFinal) {
                        return true; // Persist only final results
                    }
                }
            }
        }
        return false; // Default persist
    }

    // Lưu tin nhắn HL7 thô vào cơ sở dữ liệu
    private Hl7RawMessage saveRawMessage(Hl7MessageRequest request, String messageId, String source) {
        Hl7RawMessage rawMessage = Hl7RawMessage.builder()
                .messageId(messageId)
                .source(source)
                .payload(request.getHl7Payload().getBytes(StandardCharsets.UTF_8))
                .build();

        return rawMessageRepository.save(rawMessage);
    }

    private String decodeHl7(byte[] payload) {
        if (payload == null) return null;
        return new String(payload, StandardCharsets.UTF_8);
    }

    // Tạo bản ghi audit ingest
    private ResultIngestAudit createIngestAudit(String messageId, String rawId) {
        ResultIngestAudit audit = ResultIngestAudit.builder()
                .messageId(messageId)
                .rawId(rawId)
                .status(IngestStatus.PROCESSING)
                .build();

        return ingestAuditRepository.save(audit);
    }

    // Cập nhật TestResult tồn tại với kết quả phân tích mới
    private TestResult updateExistingResult(TestResult existing, ParsedTestResult parsed, String sourceMsgId, String sendingApp) {
        existing.setValueText(parsed.getValueText());
        existing.setUnit(parsed.getUnit());
        existing.setReferenceRange(parsed.getReferenceRange());
        existing.setAbnormalFlag(parsed.getAbnormalFlag());
        existing.setMeasuredAt(parsed.getMeasuredAt());
        existing.setSourceMsgId(sourceMsgId);
        existing.setEnteredBy(sendingApp);
        existing.setEntrySource(EntrySource.HL7);
        existing.setTestCode(parsed.getTestCode());
        return existing;
    }

    // Tạo TestResult từ kết quả phân tích
    private TestResult createTestResult(ParsedTestResult parsed, String sourceMsgId, String sendingApp, TestOrder order) {
        return TestResult.builder()
                .orderId(parsed.getOrderId())
                .testCode(parsed.getTestCode())
                .enteredBy(sendingApp)
                .entrySource(EntrySource.HL7)
                .analyteName(parsed.getAnalyteName())
                .valueText(parsed.getValueText())
                .unit(parsed.getUnit())
                .referenceRange(parsed.getReferenceRange())
                .abnormalFlag(parsed.getAbnormalFlag())
                .measuredAt(parsed.getMeasuredAt())
                .sourceMsgId(sourceMsgId)
                .orderRef(order)
                .build();
    }

    // Cập nhật audit ingest thành công
    private void updateIngestAuditSuccess(ResultIngestAudit audit) {
        audit.setStatus(IngestStatus.SUCCESS);
        ingestAuditRepository.save(audit);
    }

    // Xử lý lỗi phân tích HL7
    private Hl7ProcessResponse handleParsingError(ResultIngestAudit audit, String error, Hl7RawMessage rawMessage) {
        // Kiểm tra và xử lý audit null
        if (audit != null) {
            // Cập nhật audit ingest với trạng thái lỗi
            audit.setStatus(IngestStatus.FAILED);
            audit.setErrorMessage(error);
            ingestAuditRepository.save(audit);
        }

        // Lưu vào bảng quarantine nếu có rawMessage
        String quarantineId = null;
        if (rawMessage != null) {
            Hl7Quarantine quarantine = Hl7Quarantine.builder()
                    .messageId((audit != null) ? audit.getMessageId() : "UNKNOWN_" + System.currentTimeMillis())
                    .rawId(rawMessage.getRawId())
                    .reason("HL7 Parsing Error")
                    .details(error)
                    .build();

            quarantine = quarantineRepository.save(quarantine);
            quarantineId = quarantine.getQId();
        }

        // Trả về phản hồi lỗi
        return Hl7ProcessResponse.builder()
                .messageId(audit != null ? audit.getMessageId() : "UNKNOWN")
                .status("FAILED")
                .quarantineId(quarantineId)
                .errorMessage(error)
                .processedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }
}
