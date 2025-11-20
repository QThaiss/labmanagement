/*
 * @ {#} SampleAnalysisWorkflowServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.*;
import ca.uhn.hl7v2.parser.Parser;
import feign.FeignException;
import fit.instrument_service.client.TestOrderFeignClient;
import fit.instrument_service.client.dtos.TestOrderDetailResponse;
import fit.instrument_service.dtos.request.InitiateWorkflowRequest;
import fit.instrument_service.dtos.request.SampleInput;
import fit.instrument_service.dtos.response.SampleResponse;
import fit.instrument_service.dtos.response.WorkflowResponse;
import fit.instrument_service.entities.*;
import fit.instrument_service.enums.InstrumentStatus;
import fit.instrument_service.enums.PublishStatus;
import fit.instrument_service.enums.SampleStatus;
import fit.instrument_service.enums.WorkflowStatus;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.repositories.*;
import fit.instrument_service.services.BarcodeValidationService;
import fit.instrument_service.services.NotificationService;
import fit.instrument_service.services.ReagentCheckService;
import fit.instrument_service.services.SampleAnalysisWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @description: Service triển khai quy trình phân tích mẫu máu
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SampleAnalysisWorkflowServiceImpl implements SampleAnalysisWorkflowService {

    private final InstrumentRepository instrumentRepository;
    private final BloodSampleRepository bloodSampleRepository;
    private final RawTestResultRepository rawTestResultRepository;
    private final SampleProcessingWorkflowRepository workflowRepository;
    private final CassetteRepository cassetteRepository;
    private final BarcodeValidationService barcodeValidationService;
    private final ReagentCheckService reagentCheckService;
    private final NotificationService notificationService;
    private final TestOrderFeignClient testOrderFeignClient;
    private final Parser parser;
    private final Random random = new Random();

    @Override
    @Transactional
    public WorkflowResponse initiateWorkflow(InitiateWorkflowRequest request) {
        log.info("Initiating workflow for instrument: {}", request.getInstrumentId());

        // Kiểm tra thiết bị tồn tại
        Instrument instrument = instrumentRepository.findById(request.getInstrumentId())
                .orElseThrow(() -> new NotFoundException("Instrument not found: " + request.getInstrumentId()));

        // Kiểm tra trạng thái thiết bị nếu không phải AVAILABLE thì không thể khởi tạo quy trình
        if (instrument.getStatus() != InstrumentStatus.AVAILABLE) {
            throw new IllegalStateException("Instrument is not available for workflow execution");
        }

        // Kiểm tra hóa chất, nếu không đủ thì thông báo và dừng quy trình
        boolean reagentsAreSufficient = reagentCheckService.areReagentsSufficient(request.getInstrumentId());
        if (!reagentsAreSufficient) {
            log.error("Insufficient reagents for instrument: {}", request.getInstrumentId());
            notificationService.notifyInsufficientReagents(request.getInstrumentId());
            throw new IllegalStateException("Insufficient reagent levels. Workflow halted.");
        }

        // Tạo quy trình mới và lưu vào cơ sở dữ liệu
        SampleProcessingWorkflow workflow = new SampleProcessingWorkflow();
        workflow.setInstrumentId(request.getInstrumentId());
        workflow.setCassetteId(request.getCassetteId());
        workflow.setStatus(WorkflowStatus.INITIATED);
        workflow.setStartedAt(LocalDateTime.now());
        workflow.setReagentCheckPassed(true);
        workflow.setTestOrderServiceAvailable(true);
        workflow = workflowRepository.save(workflow);

        log.info("Created workflow: {}", workflow.getId());

        // Xử lý từng mẫu trong yêu cầu
        List<String> sampleIds = new ArrayList<>();
        for (SampleInput sampleInput : request.getSamples()) {
            // Xử lý mẫu và lưu vào cơ sở dữ liệu
            BloodSample sample = processSampleInput(sampleInput, workflow.getId(), request.getInstrumentId());
            sampleIds.add(sample.getId());
        }

        // Cập nhật danh sách mẫu vào quy trình
        workflow.setSampleIds(sampleIds);
        workflow.setStatus(WorkflowStatus.VALIDATING);
        workflow = workflowRepository.save(workflow);

        // Cập nhật trạng thái thiết bị thành RUNNING
        instrument.setStatus(InstrumentStatus.RUNNING);
        instrumentRepository.save(instrument);

        // Bắt đầu thực thi quy trình
        executeWorkflow(workflow, instrument);

        return buildWorkflowResponse(workflow);
    }

    // Hàm xử lý từng mẫu đầu vào
    private BloodSample processSampleInput(SampleInput input, String workflowId, String instrumentId) {
        log.info("Processing sample input with barcode: {}", input.getBarcode());

        // Tạo mẫu mới
        BloodSample sample = new BloodSample();
        sample.setBarcode(input.getBarcode());
        sample.setWorkflowId(workflowId);
        sample.setInstrumentId(instrumentId);
        sample.setCassetteId(input.getCassetteId());
        sample.setStatus(SampleStatus.PENDING);

        // Xác thực mã vạch, nếu không hợp lệ thì đánh dấu bỏ qua
        if (!barcodeValidationService.isValidBarcode(input.getBarcode())) {
            log.warn("Invalid barcode: {}", input.getBarcode());
            sample.setStatus(SampleStatus.SKIPPED);
            sample.setSkipReason("Invalid barcode format");
            sample = bloodSampleRepository.save(sample);
            notificationService.notifySampleStatusUpdate(sample);
            return sample;
        }

        // Xử lý đơn hàng xét nghiệm
        if (StringUtils.hasText(input.getTestOrderId())) {
            // Kiểm tra đơn hàng xét nghiệm có tồn tại không, nếu không thì tạo mới
            log.info("Checking test order ID: {}", input.getTestOrderId());
            try {
                testOrderFeignClient.getTestOrderById(input.getTestOrderId());
                sample.setTestOrderId(input.getTestOrderId());
                sample.setTestOrderAutoCreated(false);
            } catch (FeignException e) {
                log.warn("Test order not found: {}", input.getTestOrderId());
                // Create new test order
                String newTestOrderId = createTestOrder(input.getBarcode());
                sample.setTestOrderId(newTestOrderId);
                sample.setTestOrderAutoCreated(true);
            }
        } else {
            // Tạo mới đơn hàng xét nghiệm nếu không cung cấp
            log.info("No test order provided for barcode: {}. Creating new test order.", input.getBarcode());
            String newTestOrderId = createTestOrder(input.getBarcode());
            sample.setTestOrderId(newTestOrderId);
            sample.setTestOrderAutoCreated(true);
        }

        sample.setStatus(SampleStatus.VALIDATED);
        sample = bloodSampleRepository.save(sample);
        notificationService.notifySampleStatusUpdate(sample);

        return sample;
    }

    // Hàm tạo đơn hàng xét nghiệm mới
    private String createTestOrder(String barcode) {
        log.info("Creating new test order for barcode: {}", barcode);

        try {
            Map<String, Object> testOrderData = new HashMap<>();
            testOrderData.put("barcode", barcode);
            testOrderData.put("autoCreated", true);
            testOrderData.put("requiresPatientMatch", true);

            var response = testOrderFeignClient.createTestOrder(testOrderData);
            String testOrderId = (String) response.getData().get("orderId");
            log.info("Created test order: {} for barcode: {}", testOrderId, barcode);
            return testOrderId;
        } catch (FeignException e) {
            log.error("Failed to create test order for barcode: {}", barcode, e);
            // Return a placeholder ID to continue processing
            return "PENDING_" + UUID.randomUUID().toString();
        }
    }

    // Hàm thực thi quy trình
    private void executeWorkflow(SampleProcessingWorkflow workflow, Instrument instrument) {
        log.info("Executing workflow: {}", workflow.getId());

        try {
            workflow.setStatus(WorkflowStatus.RUNNING);
            workflowRepository.save(workflow);

            // Lấy danh sách mẫu đã được xác thực
            List<BloodSample> samples = bloodSampleRepository.findByWorkflowId(workflow.getId());
            List<BloodSample> validatedSamples = samples.stream()
                    .filter(s -> s.getStatus() == SampleStatus.VALIDATED)
                    .collect(Collectors.toList());

            // Đặt trạng thái mẫu thành QUEUED và thông báo
            for (BloodSample sample : validatedSamples) {
                sample.setStatus(SampleStatus.QUEUED);
                bloodSampleRepository.save(sample);
                notificationService.notifySampleStatusUpdate(sample);
            }

            // Xử lý từng mẫu
            for (BloodSample sample : validatedSamples) {
                processSample(sample);
            }

            // Cập nhật trạng thái quy trình thành COMPLETED
            workflow.setStatus(WorkflowStatus.COMPLETED);
            workflow.setCompletedAt(LocalDateTime.now());
            workflow.setResultsConvertedToHl7(true);
            workflow.setResultsPublished(true);
            workflowRepository.save(workflow);

            // Cập nhật trạng thái thiết bị thành AVAILABLE
            instrument.setStatus(InstrumentStatus.AVAILABLE);
            instrumentRepository.save(instrument);

            // Gửi thông báo hoàn thành quy trình
            notificationService.notifyWorkflowCompletion(workflow.getId(), instrument.getId());

            log.info("Workflow completed: {}", workflow.getId());

            // Check for next cassette
            processNextCassette(instrument.getId());

        } catch (Exception e) {
            log.error("Workflow execution failed: {}", workflow.getId(), e);
            workflow.setStatus(WorkflowStatus.FAILED);
            workflow.setErrorMessage(e.getMessage());
            workflowRepository.save(workflow);

            instrument.setStatus(InstrumentStatus.ERROR);
            instrumentRepository.save(instrument);
        }
    }

    // Hàm xử lý mẫ
    private void processSample(BloodSample sample) {
        log.info("Processing sample: {}", sample.getBarcode());

        // Đặt trạng thái mẫu thành PROCESSING và thông báo
        sample.setStatus(SampleStatus.PROCESSING);
        bloodSampleRepository.save(sample);
        notificationService.notifySampleStatusUpdate(sample);

        // Giả lập xử lý mẫu
        try {
            Thread.sleep(100); // Giả lập thời gian xử lý

            Map<String, String> simulatedResults = new HashMap<>();
            simulatedResults.put("WBC", String.format("%.1f", 4.0 + (random.nextDouble() * 6.0))); // vd: 7.2
            simulatedResults.put("RBC", String.format("%.1f", 3.5 + (random.nextDouble() * 2.0))); // vd: 4.8
            simulatedResults.put("HGB", String.format("%.1f", 12.0 + (random.nextDouble() * 5.0))); // vd: 14.5

            TestOrderDetailResponse orderDetails = null;
            if (!sample.getTestOrderId().startsWith("PENDING_")) {
                try {
                    // Gọi endpoint public để lấy đầy đủ chi tiết
                    orderDetails = testOrderFeignClient.getTestOrderDetailsById(sample.getTestOrderId()).getData();
                    log.info("Successfully fetched patient data for PID segment: {}", orderDetails.getFullName());
                } catch (FeignException e) {
                    log.warn("Could not fetch patient details for HL7 PID segment. Feign error: {}", e.getMessage());
                }
            } else {
                log.warn("Skipping patient detail fetch for PENDING order.");
            }

            // Chuyển đổi kết quả sang định dạng HL7
            String hl7Message = convertToHL7(sample, simulatedResults, orderDetails);

            // Xuất bản kết quả HL7
            publishResults(hl7Message, sample, simulatedResults);

            // Cập nhật trạng thái mẫu thành COMPLETED và thông báo
            sample.setStatus(SampleStatus.COMPLETED);
            bloodSampleRepository.save(sample);
            notificationService.notifySampleStatusUpdate(sample);

            log.info("Sample processing completed: {}", sample.getBarcode());
        } catch (Exception e) {
            // Xử lý lỗi trong quá trình xử lý mẫu
            log.error("Sample processing failed: {}", sample.getBarcode(), e);
            sample.setStatus(SampleStatus.FAILED);
            bloodSampleRepository.save(sample);
            notificationService.notifySampleStatusUpdate(sample);
        }
    }

    private String getHl7DateTime(LocalDateTime ldt) {
        if(ldt == null) return null;
        return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // Helper để lấy ngày theo chuẩn HL7
    private String getHl7Date(LocalDate ld) {
        if(ld == null) return null;
        return ld.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String convertToHL7(BloodSample sample, Map<String, String> results, TestOrderDetailResponse orderDetails) {
        log.debug("Converting sample results to HL7 format for barcode: {}", sample.getBarcode());

        ORU_R01 oru = new ORU_R01();
        String messageControlId = "MSG-" + sample.getBarcode() + "-" + System.currentTimeMillis();
        String now = getHl7DateTime(LocalDateTime.now());

        try {
            // MSH - Message Header (Thông tin về tin nhắn)
            MSH msh = oru.getMSH();
            msh.getFieldSeparator().setValue("|");
            msh.getEncodingCharacters().setValue("^~\\&");
            msh.getSendingApplication().getNamespaceID().setValue("INSTRUMENT_SERVICE");
            msh.getSendingFacility().getNamespaceID().setValue(sample.getInstrumentId()); // Tên máy
            msh.getReceivingApplication().getNamespaceID().setValue("TEST_ORDER_SERVICE"); // Dịch vụ nhận
            msh.getReceivingFacility().getNamespaceID().setValue("LIS"); // Hệ thống thông tin Lab
            msh.getDateTimeOfMessage().getTime().setValue(now);
            msh.getMessageType().getMessageCode().setValue("ORU"); // Observation Result (Unsolicited)
            msh.getMessageType().getTriggerEvent().setValue("R01"); // Event R01
            msh.getMessageControlID().setValue(messageControlId);
            msh.getProcessingID().getProcessingID().setValue("P"); // P = Production
            msh.getVersionID().getVersionID().setValue("2.5");

            // Định nghĩa thông tin bệnh nhân
            PID pid = oru.getPATIENT_RESULT().getPATIENT().getPID();
            pid.getSetIDPID().setValue("1");

            // Dùng Barcode làm ID bệnh nhân chính (hoặc ID bên ngoài)
            pid.getPatientIdentifierList(0).getIDNumber().setValue(sample.getBarcode());
            pid.getPatientIdentifierList(0).getAssigningAuthority().getNamespaceID().setValue("BARCODE");

            if (orderDetails != null) {
                // Nếu lấy được thông tin, điền vào
                pid.getPatientName(0).getFamilyName().getSurname().setValue(orderDetails.getFullName()); // Họ và tên
                pid.getPatientName(0).getGivenName().setValue("");
                pid.getDateTimeOfBirth().getTime().setValue(getHl7Date(orderDetails.getDateOfBirth() != null ?
                        LocalDate.parse(orderDetails.getDateOfBirth()) : null)); // Ngày sinh
                pid.getAdministrativeSex().setValue(orderDetails.getGender().name().substring(0, 1)); // M, F, O

                // Thêm Medical Record Code làm ID nội bộ
                pid.getPatientIdentifierList(1).getIDNumber().setValue(orderDetails.getMedicalRecordCode());
                pid.getPatientIdentifierList(1).getAssigningAuthority().getNamespaceID().setValue("MRN"); // Medical Record Number
            } else {
                // Nếu không lấy được, điền thông tin mặc định
                pid.getPatientName(0).getFamilyName().getSurname().setValue("UNKNOWN");
                pid.getPatientName(0).getGivenName().setValue("Patient");
            }

            // OBR - Observation Request (Thông tin về yêu cầu xét nghiệm)
            OBR obr = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBR();
            obr.getSetIDOBR().setValue("1");
            obr.getPlacerOrderNumber().getEntityIdentifier().setValue(sample.getTestOrderId());
            obr.getFillerOrderNumber().getEntityIdentifier().setValue(sample.getWorkflowId());
            obr.getUniversalServiceIdentifier().getIdentifier().setValue("PANEL-AUTO"); // Mã Panel
            obr.getUniversalServiceIdentifier().getText().setValue("Auto Panel from Instrument"); // Tên Panel
            obr.getObservationDateTime().getTime().setValue(now); // Thời gian có kết quả
            obr.getResultStatus().setValue("F"); // F = Final

            // OBX - Observation/Result (Kết quả chi tiết - lặp)
            int obxSetId = 1;
            for (Map.Entry<String, String> entry : results.entrySet()) {
                String testName = entry.getKey();
                String testValue = entry.getValue();

                // Lấy một segment OBX mới
                OBX obx = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(obxSetId - 1).getOBX();
                obx.getSetIDOBX().setValue(String.valueOf(obxSetId));
                obx.getValueType().setValue("NM"); // NM = Numeric (Kiểu số)
                obx.getObservationIdentifier().getIdentifier().setValue(testName); // Mã xét nghiệm (WBC)
                obx.getObservationIdentifier().getText().setValue(testName); // Tên xét nghiệm (WBC)

                // Gán giá trị kết quả
                obx.getObservationValue(0).parse(testValue);

                // TODO: Các đơn vị và dải tham chiếu này nên được lấy từ CSDL
                if ("WBC".equals(testName)) {
                    obx.getUnits().getIdentifier().setValue("10^9/L");
                    obx.getReferencesRange().setValue("4.0-10.0");
                } else if ("RBC".equals(testName)) {
                    obx.getUnits().getIdentifier().setValue("10^12/L");
                    obx.getReferencesRange().setValue("3.5-5.5");
                } else {
                    obx.getUnits().getIdentifier().setValue("g/dL");
                    obx.getReferencesRange().setValue("12.0-17.5");
                }

                obx.getObservationResultStatus().setValue("F"); // F = Final (Kết quả cuối)
                obx.getDateTimeOfTheObservation().getTime().setValue(now);

                obxSetId++;
            }

            // Mã hóa đối tượng HL7 thành chuỗi String
            return parser.encode(oru);

        } catch (HL7Exception e) {
            log.error("CRITICAL: Failed to generate HL7 message for barcode {}: {}", sample.getBarcode(), e.getMessage());
            // Trả về một thông điệp lỗi nếu thất bại
            return "HL7_ERROR|Failed to generate: " + e.getMessage();
        }
    }

    private void publishResults(String hl7Message, BloodSample sample, Map<String, String> rawResults) {
        log.info("Publishing HL7 results for sample: {}", sample.getBarcode());
        log.debug("HL7 Message: \n{}", hl7Message.replace("\r", "\n"));

        // === BẮT ĐẦU LƯU VÀO DATABASE ===
        try {
            RawTestResult rawResult = new RawTestResult();
            rawResult.setInstrumentId(sample.getInstrumentId());
            rawResult.setTestOrderId(sample.getTestOrderId());
            rawResult.setBarcode(sample.getBarcode());
            rawResult.setHl7Message(hl7Message);
            rawResult.setRawResultData(rawResults); // Lưu dữ liệu thô (Map)

            // Đặt trạng thái PENDING, chờ một dịch vụ khác (worker)
            // lấy từ RabbitMQ và xử lý
            rawResult.setPublishStatus(PublishStatus.PENDING);
            rawResult.setReadyForDeletion(false); // Chưa sẵn sàng để xóa

            // createdAt/createdBy sẽ được tự động điền bởi MongoCallbackConfig

            rawTestResultRepository.save(rawResult);

            log.info("Saved raw HL7 message to database with ID: {}", rawResult.getId());

            // Cập nhật cờ 'resultsPublished' trên workflow
            workflowRepository.findById(sample.getWorkflowId()).ifPresent(workflow -> {
                workflow.setResultsPublished(true); // Đánh dấu là đã lưu
                workflowRepository.save(workflow);
            });

            // TODO: Gửi tin nhắn vào RabbitMQ (ví dụ: gửi rawResult.getId())
            // Bạn đã có RabbitMQConfig, bạn sẽ dùng RabbitTemplate để gửi
            // rabbitTemplate.convertAndSend(
            //     RabbitMQConfig.EXCHANGE_NAME,
            //     RabbitMQConfig.ROUTING_KEY_RESULT, // (Giả sử bạn có routing key này)
            //     rawResult.getId() // Gửi ID để worker tự truy vấn
            // );

        } catch (Exception e) {
            log.error("Failed to save RawTestResult for sample {}: {}", sample.getBarcode(), e.getMessage(), e);

            // Nếu lưu DB thất bại, ta phải đánh dấu Sample là FAILED
            sample.setStatus(SampleStatus.FAILED);
            // sample.setSkipReason("Failed to save HL7 result"); // (Ghi chú lý do)
            bloodSampleRepository.save(sample);

            // Cập nhật Workflow là FAILED
            workflowRepository.findById(sample.getWorkflowId()).ifPresent(workflow -> {
                workflow.setStatus(WorkflowStatus.FAILED);
                workflow.setErrorMessage("Failed to save RawTestResult for sample: " + sample.getBarcode());
                workflowRepository.save(workflow);
            });
        }
    }

    @Override
    public WorkflowResponse processNextCassette(String instrumentId) {
        log.info("Processing next cassette for instrument: {}", instrumentId);

        // Lấy cassette chưa được xử lý tiếp theo trong hàng đợi
        List<Cassette> unprocessedCassettes = cassetteRepository
                .findByInstrumentIdAndProcessedOrderByQueuePositionAsc(instrumentId, false);

        // Nếu không còn cassette nào thì trả về null
        if (unprocessedCassettes.isEmpty()) {
            log.info("No cassettes in queue for instrument: {}", instrumentId);
            return null;
        }

        // Lấy cassette tiếp theo
        Cassette nextCassette = unprocessedCassettes.get(0);
        log.info("Found next cassette: {}", nextCassette.getCassetteIdentifier());

        // Lấy các mẫu máu liên quan đến cassette này và đang ở trạng thái PENDING
        List<BloodSample> cassetteSamples = bloodSampleRepository.findAll().stream()
                .filter(s -> nextCassette.getCassetteIdentifier().equals(s.getCassetteId()))
                .filter(s -> s.getStatus() == SampleStatus.PENDING)
                .collect(Collectors.toList());

        // Nếu không có mẫu nào thì đánh dấu cassette là đã xử lý và tiếp tục với cassette tiếp theo
        if (cassetteSamples.isEmpty()) {
            log.warn("No pending samples for cassette: {}", nextCassette.getCassetteIdentifier());
            nextCassette.setProcessed(true);
            nextCassette.setProcessedAt(LocalDateTime.now());
            cassetteRepository.save(nextCassette);
            return processNextCassette(instrumentId);
        }

        // Tạo yêu cầu khởi tạo quy trình cho cassette này
        InitiateWorkflowRequest request = new InitiateWorkflowRequest();
        request.setInstrumentId(instrumentId);
        request.setCassetteId(nextCassette.getCassetteIdentifier());

        // Thêm các mẫu vào quy trình xử lý
        List<SampleInput> sampleInputs = cassetteSamples.stream()
                .map(s -> {
                    SampleInput input = new SampleInput();
                    input.setBarcode(s.getBarcode());
                    input.setTestOrderId(s.getTestOrderId());
                    input.setCassetteId(s.getCassetteId());
                    return input;
                })
                .collect(Collectors.toList());
        request.setSamples(sampleInputs);

        // Đánh dấu cassette là đã được xử lý
        nextCassette.setProcessed(true);
        nextCassette.setProcessedAt(LocalDateTime.now());
        cassetteRepository.save(nextCassette);

        // Khởi tạo quy trình cho cassette này
        return initiateWorkflow(request);
    }

    @Override
    public WorkflowResponse getWorkflowStatus(String workflowId) {
        // Lấy quy trình từ cơ sở dữ liệu
        SampleProcessingWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new NotFoundException("Workflow not found: " + workflowId));
        return buildWorkflowResponse(workflow);
    }

    @Override
    public List<SampleResponse> getWorkflowSamples(String workflowId) {
        // Lấy danh sách mẫu từ cơ sở dữ liệu theo workflowId
        List<BloodSample> samples = bloodSampleRepository.findByWorkflowId(workflowId);
        return samples.stream()
                .map(this::buildSampleResponse)
                .collect(Collectors.toList());
    }

    // Hàm xây dựng phản hồi quy trình
    private WorkflowResponse buildWorkflowResponse(SampleProcessingWorkflow workflow) {
        return WorkflowResponse.builder()
                .workflowId(workflow.getId())
                .instrumentId(workflow.getInstrumentId())
                .cassetteId(workflow.getCassetteId())
                .status(workflow.getStatus())
                .sampleIds(workflow.getSampleIds())
                .startedAt(workflow.getStartedAt())
                .completedAt(workflow.getCompletedAt())
                .reagentCheckPassed(workflow.isReagentCheckPassed())
                .testOrderServiceAvailable(workflow.isTestOrderServiceAvailable())
                .errorMessage(workflow.getErrorMessage())
                .build();
    }

    // Hàm xây dựng phản hồi mẫu
    private SampleResponse buildSampleResponse(BloodSample sample) {
        return SampleResponse.builder()
                .sampleId(sample.getId())
                .barcode(sample.getBarcode())
                .testOrderId(sample.getTestOrderId())
                .workflowId(sample.getWorkflowId())
                .instrumentId(sample.getInstrumentId())
                .status(sample.getStatus())
                .isTestOrderAutoCreated(sample.isTestOrderAutoCreated())
                .skipReason(sample.getSkipReason())
                .build();
    }
}
