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
import fit.instrument_service.client.WarehouseFeignClient;
import fit.instrument_service.client.dtos.*;
import fit.instrument_service.client.dtos.ReagentLotStatusResponse;
import fit.instrument_service.client.dtos.enums.Gender;
import fit.instrument_service.configs.RabbitMQConfig;
import fit.instrument_service.dtos.request.InitiateWorkflowRequest;
import fit.instrument_service.dtos.request.SampleInput;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.SampleResponse;
import fit.instrument_service.dtos.response.WorkflowResponse;
import fit.instrument_service.entities.*;
import fit.instrument_service.enums.*;
import fit.instrument_service.events.TestResultPublishedEvent;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.repositories.*;
import fit.instrument_service.services.BarcodeValidationService;
import fit.instrument_service.services.NotificationService;
import fit.instrument_service.services.ReagentCheckService;
import fit.instrument_service.services.SampleAnalysisWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final InstrumentReagentRepository instrumentReagentRepository;
    private final SampleProcessingWorkflowRepository workflowRepository;
    private final CassetteRepository cassetteRepository;
    private final BarcodeValidationService barcodeValidationService;
    private final ReagentCheckService reagentCheckService;
    private final NotificationService notificationService;
    private final TestOrderFeignClient testOrderFeignClient;
    private final WarehouseFeignClient warehouseFeignClient;
    private final Parser parser;
    private final RabbitTemplate rabbitTemplate;
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

        // Kiểm tra tất cả mẫu có cùng cassetteId không
        List<String> cassetteIds = request.getSamples().stream()
                .map(SampleInput::getCassetteId)
                .distinct()
                .toList();

        // Nếu có nhiều hơn 1 cassetteId thì ném lỗi
        if (cassetteIds.size() > 1) {
            throw new IllegalArgumentException("All samples in a workflow must have the same cassetteId.");
        }

        String cassetteId = cassetteIds.get(0);

        // Tạo quy trình mới và lưu vào cơ sở dữ liệu
        SampleProcessingWorkflow workflow = new SampleProcessingWorkflow();
        workflow.setInstrumentId(request.getInstrumentId());
        workflow.setCassetteId(cassetteId);
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

    // Hàm xử lý từng mẫu trong request
    private BloodSample processSampleInput(SampleInput input, String workflowId, String instrumentId) {
        log.info("Processing sample input with barcode: {}", input.getBarcode());

        // Tạo mẫu mới
        BloodSample sample = new BloodSample();
        sample.setBarcode(input.getBarcode());
        sample.setWorkflowId(workflowId);
        sample.setInstrumentId(instrumentId);
        sample.setCassetteId(input.getCassetteId());

        // Đặt trạng thái ban đầu là PENDING
        sample.setStatus(SampleStatus.PENDING);

        // Kiểm tra mã vạch, nếu không hợp lệ thì đánh dấu bỏ qua
        if (!barcodeValidationService.isValidBarcode(input.getBarcode())) {
            log.warn("Invalid barcode: {}", input.getBarcode());
            sample.setStatus(SampleStatus.SKIPPED);
            sample.setSkipReason("Invalid barcode format");
            bloodSampleRepository.save(sample);
            notificationService.notifySampleStatusUpdate(sample);
            return sample;
        }

        // Xử lý TestOrder nếu được cung cấp
        if (StringUtils.hasText(input.getTestOrderId())) {
            try {
                // Lấy thông tin TestOrder từ Test Order Service
                ApiResponse<TestOrderResponse> response =
                        testOrderFeignClient.getTestOrderById(input.getTestOrderId());

                TestOrderResponse order = response != null ? response.getData() : null;

                // Kiểm tra TestOrder có tồn tại không, nếu không thì đánh dấu bỏ qua
                if (order == null || !StringUtils.hasText(order.getId())) {
                    log.error("Test order not found {}", input.getTestOrderId());
                    sample.setStatus(SampleStatus.SKIPPED);
                    sample.setSkipReason("Test order not found");
                    bloodSampleRepository.save(sample);
                    notificationService.notifySampleStatusUpdate(sample);
                    return sample;
                }

                // Kiểm tra mã vạch có khớp với TestOrder không, nếu không thì đánh dấu bỏ qua
                if (!input.getBarcode().equals(order.getBarcode())) {
                    log.error("Barcode mismatch: sample {} / order {}",
                            input.getBarcode(), order.getBarcode());
                    sample.setStatus(SampleStatus.SKIPPED);
                    sample.setSkipReason("Barcode does not match Test Order");
                    bloodSampleRepository.save(sample);
                    notificationService.notifySampleStatusUpdate(sample);
                    return sample;
                }

                // Nếu TestOrder hợp lệ, gán ID cho mẫu
                sample.setTestOrderId(order.getId());
                sample.setTestOrderAutoCreated(false);

            } catch (FeignException e) {
                log.error("Error fetching Test Order: {}", e.getMessage());
                sample.setStatus(SampleStatus.SKIPPED);
                sample.setSkipReason("Test Order Service unavailable");
                bloodSampleRepository.save(sample);
                notificationService.notifySampleStatusUpdate(sample);
                return sample;
            }

        } else {
            // Tạo mới TestOrder nếu không cung cấp
            log.info("No test order provided → Auto-create");

            String newTestOrderId = createTestOrder(input.getBarcode(), workflowId);

            sample.setTestOrderId(newTestOrderId);
            sample.setTestOrderAutoCreated(true);

            notificationService.notifyAutoCreatedTestOrder(newTestOrderId, input.getBarcode());
        }

        // Đặt trạng thái mẫu thành VALIDATED và thông báo
        sample.setStatus(SampleStatus.VALIDATED);
        bloodSampleRepository.save(sample);
        notificationService.notifySampleStatusUpdate(sample);

        return sample;
    }

    // Hàm tạo đơn hàng xét nghiệm mới
    private String createTestOrder(String barcode, String workflowId) {
        log.info("Auto-creating TestOrder for barcode: {}", barcode);

        try {
            AutoCreateTestOrderRequest request = new AutoCreateTestOrderRequest();
            request.setBarcode(barcode);
            request.setAutoCreated(true);
            request.setRequiresPatientMatch(true);

            // Gọi API auto-create từ Test Order Service
            ApiResponse<TestOrderResponse> response =
                    testOrderFeignClient.autoCreateTestOrder(request);

            TestOrderResponse data = response != null ? response.getData() : null;
            log.debug("Auto-create response data: {}", data);
            String testOrderId = (data != null ? data.getId() : null);

            // Nếu không nhận được ID thì tạo ID giả
            if (!StringUtils.hasText(testOrderId)) {
                log.warn("Auto-create returned empty ID for barcode: {}", barcode);
                testOrderId = "AUTO_CREATE" + UUID.randomUUID();
            }

            log.info("Auto-created TestOrder: {} for barcode: {}", testOrderId, barcode);
            return testOrderId;

        } catch (FeignException e) {
            log.error("Failed to auto-create TestOrder for barcode {}. Cause: {}", barcode, e.getMessage());

            // Đánh dấu dịch vụ Test Order không khả dụng
            markTestOrderServiceUnavailable(workflowId);
            return "AUTO_CREATE" + UUID.randomUUID();
        }
    }

    // Hàm đánh dấu dịch vụ Test Order không khả dụng trong workflow
    private void markTestOrderServiceUnavailable(String workflowId) {
        workflowRepository.findById(workflowId).ifPresent(workflow -> {
            workflow.setTestOrderServiceAvailable(false);
            workflowRepository.save(workflow);
        });
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
                    .toList();

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

    // Hàm xử lý mẫu
    private void processSample(BloodSample sample) {
        log.info("Processing sample: {}", sample.getBarcode());

        // Đặt trạng thái mẫu thành PROCESSING và thông báo
        sample.setStatus(SampleStatus.PROCESSING);
        bloodSampleRepository.save(sample);
        notificationService.notifySampleStatusUpdate(sample);

        // Giả lập xử lý mẫu
        try {
            Thread.sleep(100); // Giả lập thời gian xử lý

            // Giảm hóa chất sử dụng cho mẫu
            deductReagents(sample.getInstrumentId());

            // Lấy chi tiết đơn hàng xét nghiệm
            TestOrderResponse orderDetails = fetchTestOrderDetails(sample);
            // Mô phỏng kết quả xét nghiệm
            Map<TestParameterResponse, Double> simulatedResults = simulateResults(orderDetails);
            // Lưu kết quả thô vào cơ sở dữ liệu
            Map<String, String> rawResults = simulatedResults.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getAbbreviation(),
                            entry -> formatResultValue(entry.getValue()),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));

            // Chuyển đổi kết quả sang định dạng HL7
            String hl7Message = convertToHL7(sample, simulatedResults, orderDetails);

            // Xuất bản kết quả HL7
            publishResults(hl7Message, sample, rawResults);

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

    // Hàm giảm hóa chất sử dụng cho mẫu
    private void deductReagents(String instrumentId) {
        log.info("Deducting reagents for instrument: {}", instrumentId);

        // Lấy tất cả lô hóa chất đang In Use
        List<InstrumentReagent> reagents =
                instrumentReagentRepository.findByInstrumentId(instrumentId)
                        .stream()
                        .filter(r -> r.getStatus() == ReagentStatus.IN_USE)
                        .toList();

        // Nếu không có hóa chất nào thì thông báo và dừng quy trình
        if (reagents.isEmpty()) {
            log.error("No reagent in use for instrument {}", instrumentId);
            notificationService.notifyInsufficientReagents(instrumentId);
            throw new IllegalStateException("No reagent available for this instrument");
        }

        // Giảm mỗi reagent theo định mức sử dụng mỗi lần chạy
        for (InstrumentReagent reagent : reagents) {
            int usageAmount = determineUsageAmount(reagent);
            int oldQuantity = Optional.ofNullable(reagent.getQuantity()).orElse(0);
            int newQuantity = Math.max(0, oldQuantity - usageAmount);

            reagent.setQuantity(newQuantity);
            instrumentReagentRepository.save(reagent);

            log.info("Reagent {} deducted by {}: {} -> {}", reagent.getReagentName(), usageAmount, oldQuantity, newQuantity);

            // Nếu hết hóa chất thì thông báo + chuyển trạng thái máy
            if (newQuantity == 0) {
                notificationService.notifyReagentEmpty(instrumentId, reagent.getReagentName());

                instrumentRepository.findById(instrumentId).ifPresent(inst -> {
                    inst.setStatus(InstrumentStatus.ERROR);
                    instrumentRepository.save(inst);
                });

                throw new IllegalStateException("Reagent exhausted: " + reagent.getReagentName());
            }
        }
    }

    // Hàm xác định mức sử dụng hóa chất cho mỗi mẫu
    private int determineUsageAmount(InstrumentReagent reagent) {
        // Lấy mức sử dụng mỗi lần chạy từ Warehouse Service
        String usageDescriptor = fetchUsagePerRun(reagent.getLotNumber());

        // Nếu không có mô tả mức sử dụng thì mặc định là 1
        if (!StringUtils.hasText(usageDescriptor)) {
            log.warn("Usage per run is unavailable for lot {}. Defaulting deduction to 1.", reagent.getLotNumber());
            return 1;
        }

        // Phân tích mô tả mức sử dụng để lấy số lượng
        return parseUsageAmount(usageDescriptor)
                // Làm tròn lên và đảm bảo ít nhất là 1
                .map(amount -> (int) Math.max(1, Math.ceil(amount)))
                // Nếu không thể phân tích được thì mặc định là 1
                .orElseGet(() -> {
                    log.warn("Could not parse usage per run '{}' for lot {}. Defaulting deduction to 1.",
                            usageDescriptor, reagent.getLotNumber());
                    return 1;
                });
    }

    // Hàm phân tích mô tả mức sử dụng để lấy số lượng
    private Optional<Double> parseUsageAmount(String usageDescriptor) {
        try {
            // Sử dụng regex để tìm số trong chuỗi
            Matcher matcher = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)").matcher(usageDescriptor);

            // Nếu tìm thấy thì trả về số đã phân tích
            if (matcher.find()) {
                return Optional.of(Double.parseDouble(matcher.group(1)));
            }
        } catch (Exception e) {
            log.error("Error parsing usage descriptor '{}': {}", usageDescriptor, e.getMessage());
        }

        return Optional.empty();
    }

    // Hàm lấy mức sử dụng hóa chất cho mỗi lần chạy từ Warehouse Service
    private String fetchUsagePerRun(String lotNumber) {
        try {
            // Gọi API từ Warehouse Service để lấy trạng thái lô hóa chất
            ApiResponse<ReagentLotStatusResponse> response = warehouseFeignClient.getReagentLotStatus(lotNumber);

            // Kiểm tra phản hồi và trả về mức sử dụng mỗi lần chạy nếu có
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getUsagePerRun();
            }

            log.warn("Usage per run missing for lot {} from warehouse response.", lotNumber);
        } catch (FeignException e) {
            log.error("Failed to fetch reagent lot status for {}: {}", lotNumber, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while fetching reagent usage for {}: {}", lotNumber, e.getMessage());
        }

        return null;
    }

    // Hàm lấy chi tiết đơn hàng xét nghiệm
    private TestOrderResponse fetchTestOrderDetails(BloodSample sample) {
        if (!StringUtils.hasText(sample.getTestOrderId()) || sample.getTestOrderId().startsWith("AUTO_CREATE_")) {
            log.warn("Skipping patient detail fetch for AUTO_CREATE or missing order.");
            return null;
        }

        try {
            ApiResponse<TestOrderResponse> response = testOrderFeignClient.getTestOrderById(sample.getTestOrderId());
            TestOrderResponse orderDetails = response != null ? response.getData() : null;
            if (orderDetails != null) {
                log.info("Successfully fetched patient data for PID segment: {}", orderDetails.getFullName());
            }
            return orderDetails;
        } catch (FeignException e) {
            log.warn("Could not fetch patient details for HL7 PID segment. Feign error: {}", e.getMessage());
            markTestOrderServiceUnavailable(sample.getWorkflowId());
            return null;
        }
    }

    // Hàm mô phỏng kết quả xét nghiệm
    private Map<TestParameterResponse, Double> simulateResults(TestOrderResponse orderDetails) {
        List<TestParameterResponse> parameters = Optional.ofNullable(orderDetails)
                .map(TestOrderResponse::getTestType)
                .map(TestTypeResponse::getTestParameters)
                .filter(list -> !list.isEmpty())
                .orElseGet(this::defaultHematologyParameters);

        Map<TestParameterResponse, Double> results = new LinkedHashMap<>();
        for (TestParameterResponse parameter : parameters) {
            double value = generateValueForParameter(parameter, orderDetails != null ? orderDetails.getGender() : null);
            results.put(parameter, value);
        }
        return results;
    }

    // Hàm sinh giá trị kết quả cho từng tham số
    private double generateValueForParameter(TestParameterResponse parameter, Gender gender) {
        ParameterRangeResponse range = selectRange(parameter, gender);
        double value;
        if (range != null && range.getMinValue() != null && range.getMaxValue() != null) {
            double min = range.getMinValue();
            double max = range.getMaxValue();
            double span = Math.max(0.1, max - min);
            if (random.nextDouble() < 0.7) {
                value = min + span * random.nextDouble();
            } else if (random.nextBoolean()) {
                value = max + span * (0.1 + random.nextDouble() * 0.4);
            } else {
                value = Math.max(0, min - span * (0.1 + random.nextDouble() * 0.4));
            }
        } else {
            value = 1 + random.nextDouble() * 10;
        }
        return Double.parseDouble(formatResultValue(value));
    }

    // Hàm chọn dải tham số phù hợp với giới tính
    private ParameterRangeResponse selectRange(TestParameterResponse parameter, Gender gender) {
        if (parameter.getParameterRanges() == null || parameter.getParameterRanges().isEmpty()) {
            return null;
        }

        return parameter.getParameterRanges().stream()
                .filter(range -> genderMatches(range.getGender(), gender))
                .findFirst()
                .orElse(parameter.getParameterRanges().get(0));
    }

    // Hàm kiểm tra giới tính có khớp với dải tham số không
    private boolean genderMatches(String rangeGender, Gender gender) {
        if (!StringUtils.hasText(rangeGender)) {
            return true;
        }
        if (gender == null) {
            return "BOTH".equalsIgnoreCase(rangeGender);
        }
        if ("BOTH".equalsIgnoreCase(rangeGender)) {
            return true;
        }
        return rangeGender.equalsIgnoreCase(gender.name());
    }

    // Hàm định dạng giá trị kết quả
    private String formatResultValue(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.1f", value);
    }

    // Hàm cung cấp tham số mặc định cho xét nghiệm huyết học
    private List<TestParameterResponse> defaultHematologyParameters() {
        List<TestParameterResponse> defaults = new ArrayList<>();

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-WBC")
                .paramName("White Blood Cell Count")
                .abbreviation("WBC")
                .description("Measures the number of white blood cells (leukocytes) in the blood, which helps fight infection.")
                .parameterRanges(List.of(ParameterRangeResponse.builder()
                        .gender("BOTH")
                        .minValue(4000.0)
                        .maxValue(10000.0)
                        .unit("cells/µL")
                        .build()))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-RBC")
                .paramName("Red Blood Cell Count")
                .abbreviation("RBC")
                .description("Measures the number of red blood cells per unit of blood, which are responsible for carrying oxygen throughout the body.")
                .parameterRanges(Arrays.asList(
                        ParameterRangeResponse.builder().gender("MALE").minValue(4.7).maxValue(6.1).unit("million/µL").build(),
                        ParameterRangeResponse.builder().gender("FEMALE").minValue(4.2).maxValue(5.4).unit("million/µL").build()
                ))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-HGB")
                .paramName("Hemoglobin")
                .abbreviation("Hb/HGB")
                .description("Measures the amount of hemoglobin in the blood, which is the protein in red blood cells that carries oxygen.")
                .parameterRanges(Arrays.asList(
                        ParameterRangeResponse.builder().gender("MALE").minValue(14.0).maxValue(18.0).unit("g/dL").build(),
                        ParameterRangeResponse.builder().gender("FEMALE").minValue(12.0).maxValue(16.0).unit("g/dL").build()
                ))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-HCT")
                .paramName("Hematocrit")
                .abbreviation("HCT")
                .description("Represents the percentage of red blood cells in the blood volume, indicating oxygen-carrying capacity.")
                .parameterRanges(Arrays.asList(
                        ParameterRangeResponse.builder().gender("MALE").minValue(42.0).maxValue(52.0).unit("%").build(),
                        ParameterRangeResponse.builder().gender("FEMALE").minValue(37.0).maxValue(47.0).unit("%").build()
                ))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-PLT")
                .paramName("Platelet Count")
                .abbreviation("PLT")
                .description("Measures the number of platelets in the blood, which are responsible for clotting.")
                .parameterRanges(List.of(ParameterRangeResponse.builder()
                        .gender("BOTH")
                        .minValue(150000.0)
                        .maxValue(350000.0)
                        .unit("cells/µL")
                        .build()))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-MCV")
                .paramName("Mean Corpuscular Volume")
                .abbreviation("MCV")
                .description("Indicates the average size of red blood cells.")
                .parameterRanges(List.of(ParameterRangeResponse.builder()
                        .gender("BOTH")
                        .minValue(80.0)
                        .maxValue(100.0)
                        .unit("fL")
                        .build()))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-MCH")
                .paramName("Mean Corpuscular Haemoglobin")
                .abbreviation("MCH")
                .description("Represents the average amount of haemoglobin per red blood cell.")
                .parameterRanges(List.of(ParameterRangeResponse.builder()
                        .gender("BOTH")
                        .minValue(27.0)
                        .maxValue(33.0)
                        .unit("pg")
                        .build()))
                .build());

        defaults.add(TestParameterResponse.builder()
                .testParameterId("TP-MCHC")
                .paramName("Mean Corpuscular Haemoglobin Concentration")
                .abbreviation("MCHC")
                .description("Calculates the average concentration of haemoglobin in red blood cells.")
                .parameterRanges(List.of(ParameterRangeResponse.builder()
                        .gender("BOTH")
                        .minValue(32.0)
                        .maxValue(36.0)
                        .unit("g/dL")
                        .build()))
                .build());

        return defaults;
    }

    // Helper để lấy ngày giờ theo chuẩn HL7
    private String getHl7DateTime(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // Helper để lấy ngày theo chuẩn HL7
    private String getHl7Date(LocalDate ld) {
        if (ld == null) return null;
        return ld.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String convertToHL7(BloodSample sample, Map<TestParameterResponse, Double> results, TestOrderResponse orderDetails) {
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
                if (orderDetails.getGender() != null) {
                    pid.getAdministrativeSex().setValue(orderDetails.getGender().name().substring(0, 1)); // M, F, O
                }
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
            for (Map.Entry<TestParameterResponse, Double> entry : results.entrySet()) {
                TestParameterResponse parameter = entry.getKey();
                String abbreviation = StringUtils.hasText(parameter.getAbbreviation()) ? parameter.getAbbreviation() : parameter.getParamName();
                String parameterName = StringUtils.hasText(parameter.getParamName()) ? parameter.getParamName() : abbreviation;

                // Lấy một segment OBX mới
                OBX obx = oru.getPATIENT_RESULT().getORDER_OBSERVATION().getOBSERVATION(obxSetId - 1).getOBX();
                obx.getSetIDOBX().setValue(String.valueOf(obxSetId));
                obx.getValueType().setValue("NM"); // NM = Numeric (Kiểu số)
                obx.getObservationIdentifier().getIdentifier().setValue(abbreviation); // Mã xét nghiệm (WBC)
                obx.getObservationIdentifier().getText().setValue(parameterName); // Tên xét nghiệm (WBC)

                obx.getObservationValue(0).parse(formatResultValue(entry.getValue()));

                ParameterRangeResponse range = selectRange(parameter, orderDetails != null ? orderDetails.getGender() : null);
                if (range != null && StringUtils.hasText(range.getUnit())) {
                    obx.getUnits().getIdentifier().setValue(range.getUnit());
                }
                if (range != null && range.getMinValue() != null && range.getMaxValue() != null) {
                    obx.getReferencesRange().setValue(String.format("%.1f-%.1f", range.getMinValue(), range.getMaxValue()));
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

        // Luu kết quả thô vào cơ sở dữ liệu
        RawTestResult rawResult = new RawTestResult();
        rawResult.setInstrumentId(sample.getInstrumentId());
        rawResult.setTestOrderId(sample.getTestOrderId());
        rawResult.setBarcode(sample.getBarcode());
        rawResult.setHl7Message(hl7Message);
        rawResult.setRawResultData(rawResults);
        rawResult.setPublishStatus(PublishStatus.PENDING);
        rawResult.setReadyForDeletion(false);

        try {
            rawResult = rawTestResultRepository.save(rawResult);
            log.info("Saved raw HL7 message to database with ID: {}", rawResult.getId());
        } catch (Exception e) {
            handleRawResultPersistenceFailure(sample, e);
            return;
        }

        // --- BỔ SUNG MỚI: Gửi Event sang RabbitMQ cho Monitoring Service ---
        // Routing Key phải khớp với cái mà Monitoring Service đang lắng nghe ("instrument.test_result")
        String routingKey = "instrument.test_result";

        try {
            TestResultPublishedEvent event = TestResultPublishedEvent.builder()
                    .instrumentId(sample.getInstrumentId())
                    .testOrderId(sample.getTestOrderId())
                    .barcode(sample.getBarcode())
                    .hl7Message(hl7Message)
                    .rawResultData(rawResults)
                    .publishedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INSTRUMENT_EXCHANGE, // Exchange đã cấu hình
                    routingKey,
                    event
            );
            log.info("Successfully published TestResult event to RabbitMQ for Monitoring Service");
        } catch (Exception e) {
            log.error("Failed to publish TestResult event to RabbitMQ", e);
            rawResult.setPublishStatus(PublishStatus.FAILED);
            rawResult.setReadyForDeletion(false);
            rawTestResultRepository.save(rawResult);
            return;
        }
        // ------------------------------------------------------------------

//        boolean publishedToTestOrder = publishToTestOrderService(hl7Message);
//
//
//        PublishStatus publishStatus = (publishedToTestOrder)
//                ? PublishStatus.SENT
//                : PublishStatus.FAILED;
//
//        rawResult.setPublishStatus(publishStatus);
//        rawResult.setReadyForDeletion(publishedToTestOrder);
//        rawTestResultRepository.save(rawResult);

        workflowRepository.findById(sample.getWorkflowId()).ifPresent(workflow -> {
            workflow.setResultsPublished(true);
            workflowRepository.save(workflow);
        });
    }

    private boolean publishToTestOrderService(String hl7Message) {
        try {
            ApiResponse<Hl7ProcessResponse> response = testOrderFeignClient.publishHl7Result(hl7Message);
            boolean success = response != null && Boolean.TRUE.equals(response.isSuccess());
            if (!success) {
                log.info("Test Order Service returned unsuccessful status for HL7 publish");
            }
            return success;
        } catch (Exception e) {
            log.error("Failed to publish HL7 results to Test Order Service: {}", e.getMessage());
            return false;
        }
    }

    private void handleRawResultPersistenceFailure(BloodSample sample, Exception e) {
        log.error("Failed to save RawTestResult for sample {}: {}", sample.getBarcode(), e.getMessage(), e);

        sample.setStatus(SampleStatus.FAILED);
        bloodSampleRepository.save(sample);

        workflowRepository.findById(sample.getWorkflowId()).ifPresent(workflow -> {
            workflow.setStatus(WorkflowStatus.FAILED);
            workflow.setErrorMessage("Failed to save RawTestResult for sample: " + sample.getBarcode());
            workflowRepository.save(workflow);
        });
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
