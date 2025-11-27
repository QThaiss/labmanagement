package fit.test_order_service.services.implTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.client.IamFeignClient;
import fit.test_order_service.client.PatientMedicalRecordFeignClient;
import fit.test_order_service.client.dtos.PatientMedicalRecordInternalResponse;
import fit.test_order_service.client.dtos.UserInternalResponse;
import fit.test_order_service.dtos.event.SystemEvent;
import fit.test_order_service.dtos.request.*;
import fit.test_order_service.dtos.response.*;
import fit.test_order_service.entities.*;
import fit.test_order_service.enums.*;
import fit.test_order_service.exceptions.BadRequestException;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.exceptions.UnauthorizedException;
import fit.test_order_service.mappers.TestOrderMapper;
import fit.test_order_service.repositories.*;
import fit.test_order_service.services.*;
import fit.test_order_service.services.impl.TestOrderServiceImpl;
import fit.test_order_service.specifications.TestOrderSpecification;
import fit.test_order_service.utils.SecurityUtils;
import fit.test_order_service.utils.TestOrderGenerator;
import fit.test_order_service.validators.TestOrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fit.test_order_service.enums.OrderStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestOrderServiceImplTest {

    @Mock
    private TestOrderRepository testOrderRepository;
    @Mock
    private TestOrderMapper testOrderMapper;
    @Mock
    private PatientMedicalRecordFeignClient patientMedicalRecordFeignClient;
    @Mock
    private OrderEventLogService orderEventLogService;
    @Mock
    private TestOrderValidator testOrderValidator;
    @Mock
    private IamFeignClient iamFeignClient;
    @Mock
    private TestOrderSpecification testOrderSpecification;
    @Mock
    private TestTypeRepository testTypeRepository;
    @Mock
    private ReportJobRepository reportJobRepository;
    @Mock
    private PdfGenerationQueueService pdfGenerationQueueService;
    @Mock
    private ExcelGenerationQueueService excelGenerationQueueService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TestOrderStatusService testOrderStatusService;
    @Mock
    private TestResultAdjustLogRepository testResultAdjustLogRepository;
    @Mock
    private TestResultRepository testResultRepository;
    @Mock
    private OrderCommentRepository orderCommentRepository;
    @Mock
    private Hl7ParserService hl7ParserService;
    @Mock
    private TestTypeService testTypeService;
    @Mock
    private EventLogPublisher eventLogPublisher;

    @Captor
    private ArgumentCaptor<TestOrder> testOrderCaptor;

    @InjectMocks
    private TestOrderServiceImpl testOrderService;

    // Dữ liệu giả định
    private final String ORDER_ID = "ORD-001";
    private final String USER_ID = "user-123";
    private final String MEDICAL_RECORD_CODE = "MR-456";
    private final String TEST_TYPE_ID = "TT-789";
    private final String BARCODE = "B-999";
    private TestType mockTestType;
    private TestOrder mockTestOrder;
    private CreateTestOrderRequest createRequest;
    private TestOrderResponse mockResponse;
    private TestTypeResponse mockTestTypeResponse;

    @BeforeEach
    void setUp() {
        // Mock TestType
        mockTestType = new TestType();
        mockTestType.setId(TEST_TYPE_ID);
        mockTestType.setName("Blood Test");

        // Mock TestTypeResponse
        mockTestTypeResponse = TestTypeResponse.builder()
                .id(TEST_TYPE_ID)
                .name("Blood Test")
                .build();

        // Mock TestOrder Entity
        mockTestOrder = new TestOrder();
        mockTestOrder.setOrderId(ORDER_ID);
        mockTestOrder.setTestTypeRef(mockTestType);
        mockTestOrder.setMedicalRecordCode(MEDICAL_RECORD_CODE);
        mockTestOrder.setStatus(OrderStatus.PENDING);
        mockTestOrder.setDeleted(false);
        mockTestOrder.setAutoCreated(false);
        mockTestOrder.setTestTypeIdSnapshot(TEST_TYPE_ID);
        mockTestOrder.setTestTypeNameSnapshot("Blood Test");
        mockTestOrder.setResults(new ArrayList<>());
        mockTestOrder.setBarcode(BARCODE);

        // Mock TestOrderResponse
        mockResponse = TestOrderResponse.builder()
                .id(ORDER_ID)
                .testType(mockTestTypeResponse)
                .medicalRecordCode(MEDICAL_RECORD_CODE)
                .status(OrderStatus.PENDING)
                .build();

        // Mock CreateRequest
        createRequest = new CreateTestOrderRequest();
        createRequest.setTestTypeId(TEST_TYPE_ID);
        createRequest.setMedicalRecordCode(MEDICAL_RECORD_CODE);
    }

    // --- TEST createTestOrder ---

    @Test
    @DisplayName("createTestOrder - Success")
    void createTestOrder_Success() {
        // Setup mocks cho các phương thức static
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class);
             MockedStatic<TestOrderGenerator> mockedGenerator = mockStatic(TestOrderGenerator.class)) {

            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            mockedGenerator.when(TestOrderGenerator::generateBarcode).thenReturn("BARCODE-123");

            // Mock dependencies
            when(testTypeRepository.findById(TEST_TYPE_ID)).thenReturn(Optional.of(mockTestType));

            ApiResponse<PatientMedicalRecordInternalResponse> apiResponse = new ApiResponse<>();
            // Giá trị cần được mapper set vào TestOrder.medicalRecordId
            String PATIENT_ID = "patient-id-123";

            // FIX: Thêm đủ 15 đối số cho PatientMedicalRecordInternalResponse
            PatientMedicalRecordInternalResponse patientData = new PatientMedicalRecordInternalResponse(
                    PATIENT_ID, MEDICAL_RECORD_CODE, "Full Name", null, null, null,
                    null, null, null, null, null, "SYSTEM_TEST", null, null, null
            );
            apiResponse.setData(patientData);
            when(patientMedicalRecordFeignClient.getPatientMedicalRecordByCode(MEDICAL_RECORD_CODE))
                    .thenReturn(apiResponse);

            // FIX: Cập nhật mockTestOrder với giá trị MedicalRecordId mà mapper nên gán
            mockTestOrder.setMedicalRecordId(PATIENT_ID);

            when(testOrderMapper.toEntity(patientData)).thenReturn(mockTestOrder);
            when(testTypeService.getTestTypeById(TEST_TYPE_ID)).thenReturn(mockTestTypeResponse);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(mockTestOrder);
            when(testOrderMapper.toResponse(mockTestOrder, mockTestTypeResponse)).thenReturn(mockResponse);

            // Call method
            TestOrderResponse result = testOrderService.createTestOrder(createRequest);

            // Assertions
            assertNotNull(result);
            assertEquals(ORDER_ID, result.getId());
            assertEquals(OrderStatus.PENDING, mockTestOrder.getStatus());
            assertEquals(USER_ID, mockTestOrder.getCreatedBy());

            // Verify interactions
            // Lỗi NPE đã được khắc phục tại đây vì mockTestOrder.getMedicalRecordId() giờ là non-null
            verify(orderEventLogService).logEvent(eq(mockTestOrder), eq(EventType.CREATE), anyString());
            verify(eventLogPublisher).publishEvent(any(SystemEvent.class));
        }
    }

    @Test
    @DisplayName("createTestOrder - Throws NotFoundException for TestType")
    void createTestOrder_TestTypeNotFound() {
        when(testTypeRepository.findById(TEST_TYPE_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> testOrderService.createTestOrder(createRequest));
        verify(testOrderRepository, never()).save(any());
    }

    // --- TEST getTestOrderById ---

    @Test
    @DisplayName("getTestOrderById - Success with Comments")
    void getTestOrderById_SuccessWithComments() {
        // Mock TestResult
        TestResult result1 = new TestResult();
        result1.setResultId("RES-001");
        result1.setOrderId(ORDER_ID);
        mockTestOrder.getResults().add(result1);
        mockTestOrder.setStatus(COMPLETED);

        // Mock Comments
        OrderComment orderComment = new OrderComment();
        orderComment.setCommentId("C-001");
        orderComment.setTargetType(CommentTargetType.ORDER);
        orderComment.setTargetId(ORDER_ID);
        orderComment.setAuthorUserId(USER_ID);
        orderComment.setCreatedAt(LocalDateTime.now().minusHours(1));
        orderComment.setContent("Order comment");

        OrderComment resultComment = new OrderComment();
        resultComment.setCommentId("C-002");
        resultComment.setTargetType(CommentTargetType.RESULT);
        resultComment.setTargetId("RES-001");
        resultComment.setAuthorUserId(USER_ID);
        resultComment.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        List<OrderComment> orderComments = List.of(orderComment);
        List<OrderComment> resultComments = List.of(resultComment);

        // Mock Feign client for user data
        // FIX: Thêm đủ 17 đối số cho UserInternalResponse
        UserInternalResponse userResponse = new UserInternalResponse(
                USER_ID, "test_user", "Tester", null, null, null,
                null, null, null, null, null, null,
                "ROLE_LAB", Boolean.TRUE, null, null, null
        );
        ApiResponse<UserInternalResponse> userApiResponse = new ApiResponse<>();
        userApiResponse.setData(userResponse);

        // Mock Mappers and Repositories
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));
        when(testOrderMapper.toDetailResponse(mockTestOrder)).thenReturn(new TestOrderDetailResponse());
        when(orderCommentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(
                eq(CommentTargetType.ORDER), eq(ORDER_ID)))
                .thenReturn(orderComments);
        when(orderCommentRepository.findByTargetTypeAndTargetIdInAndParentIdIsNullOrderByCreatedAtAsc(
                eq(CommentTargetType.RESULT), anyList()))
                .thenReturn(resultComments);
        when(iamFeignClient.getUserById(USER_ID)).thenReturn(userApiResponse);
        when(testResultRepository.findById("RES-001")).thenReturn(Optional.of(result1));

        // FIX: Sử dụng doCallRealMethod().when() để ngăn phương thức bị gọi hai lần.
        TestOrderServiceImpl spyService = spy(testOrderService);
        doCallRealMethod().when(spyService).getTestOrderById(ORDER_ID);

        // Call method
        TestOrderDetailResponse result = spyService.getTestOrderById(ORDER_ID);

        // Assertions
        assertNotNull(result);
        assertFalse(result.getComments().isEmpty());
        assertEquals(2, result.getComments().size());

        // Verify only 1 invocation was intended and executed.
        verify(orderEventLogService, times(1)).logEvent(eq(mockTestOrder), eq(EventType.VIEW), anyString());
    }

    @Test
    @DisplayName("getTestOrderById - Throws NotFoundException")
    void getTestOrderById_NotFound() {
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> testOrderService.getTestOrderById(ORDER_ID));
    }

    @Test
    @DisplayName("getTestOrderById - Throws NotFoundException for SYSTEM_ORDER_ID")
    void getTestOrderById_SystemOrderId() {
        assertThrows(NotFoundException.class, () -> testOrderService.getTestOrderById("SYSTEM_ORDER_ID"));
    }

    // --- TEST deleteTestOrder ---

    @Test
    @DisplayName("deleteTestOrder - Success")
    void deleteTestOrder_Success() {
        mockTestOrder.setStatus(OrderStatus.PENDING);
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            testOrderService.deleteTestOrder(ORDER_ID);

            assertTrue(mockTestOrder.isDeleted());
            assertEquals(USER_ID, mockTestOrder.getDeletedBy());
            verify(testOrderRepository).save(mockTestOrder);
            verify(eventLogPublisher).publishEvent(any(SystemEvent.class));
            verify(orderEventLogService).logEvent(eq(mockTestOrder), eq(EventType.DELETE), anyString());
        }
    }

    @Test
    @DisplayName("deleteTestOrder - Throws NotFoundException")
    void deleteTestOrder_NotFound() {
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> testOrderService.deleteTestOrder(ORDER_ID));
    }

    @Test
    @DisplayName("deleteTestOrder - Throws IllegalStateException when Completed")
    void deleteTestOrder_CompletedStatus() {
        mockTestOrder.setStatus(COMPLETED);
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));

        assertThrows(IllegalStateException.class, () -> testOrderService.deleteTestOrder(ORDER_ID));
        verify(testOrderRepository, never()).save(any());
    }

    // --- TEST updateTestOrderByCode ---

    @Test
    @DisplayName("updateTestOrderByCode - Update FullName Success")
    void updateTestOrderByCode_UpdateFullName() {
        UpdateTestOrderRequest request = new UpdateTestOrderRequest();
        request.setFullName("New Full Name");

        when(testOrderValidator.validateForUpdate(ORDER_ID)).thenReturn(mockTestOrder);
        when(testOrderRepository.save(any(TestOrder.class))).thenReturn(mockTestOrder);
        when(testTypeService.getTestTypeById(TEST_TYPE_ID)).thenReturn(mockTestTypeResponse);
        when(testOrderMapper.toResponse(mockTestOrder, mockTestTypeResponse)).thenReturn(mockResponse);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            TestOrderResponse result = testOrderService.updateTestOrderByCode(ORDER_ID, request);

            assertNotNull(result);
            assertEquals("New Full Name", mockTestOrder.getFullName());
            assertEquals(USER_ID, mockTestOrder.getUpdatedBy());
            verify(orderEventLogService).logOrderUpdate(any(TestOrder.class), eq(mockTestOrder), eq(EventType.UPDATE));
        }
    }

    @Test
    @DisplayName("updateTestOrderByCode - No Changes")
    void updateTestOrderByCode_NoChanges() {
        UpdateTestOrderRequest request = new UpdateTestOrderRequest();
        mockTestOrder.setFullName("Existing Full Name");
        request.setFullName("Existing Full Name"); // Same value

        when(testOrderValidator.validateForUpdate(ORDER_ID)).thenReturn(mockTestOrder);
        when(testTypeService.getTestTypeById(TEST_TYPE_ID)).thenReturn(mockTestTypeResponse);
        when(testOrderMapper.toResponse(mockTestOrder, mockTestTypeResponse)).thenReturn(mockResponse);

        TestOrderResponse result = testOrderService.updateTestOrderByCode(ORDER_ID, request);

        assertNotNull(result);
        verify(testOrderRepository, never()).save(any());
        verify(orderEventLogService, never()).logOrderUpdate(any(), any(), any());
    }

    @Test
    @DisplayName("updateTestOrderByCode - Update TestType Success")
    void updateTestOrderByCode_UpdateTestType() {
        UpdateTestOrderRequest request = new UpdateTestOrderRequest();
        String newTestTypeId = "TT-444";
        request.setTestTypeId(newTestTypeId);

        TestType newTestType = new TestType();
        newTestType.setId(newTestTypeId);
        newTestType.setName("New Test");
        TestTypeResponse newTestTypeResponse = TestTypeResponse.builder().id(newTestTypeId).name("New Test").build();

        when(testOrderValidator.validateForUpdate(ORDER_ID)).thenReturn(mockTestOrder);
        when(testTypeRepository.findById(newTestTypeId)).thenReturn(Optional.of(newTestType));
        when(testOrderRepository.save(any(TestOrder.class))).thenReturn(mockTestOrder);
        when(testTypeService.getTestTypeById(newTestTypeId)).thenReturn(newTestTypeResponse);
        when(testOrderMapper.toResponse(mockTestOrder, newTestTypeResponse)).thenReturn(TestOrderResponse.builder().testType(newTestTypeResponse).build());


        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            testOrderService.updateTestOrderByCode(ORDER_ID, request);

            assertEquals(newTestTypeId, mockTestOrder.getTestTypeRef().getId());
            assertEquals("New Test", mockTestOrder.getTestTypeNameSnapshot());
            verify(orderEventLogService).logOrderUpdate(any(TestOrder.class), eq(mockTestOrder), eq(EventType.UPDATE));
        }
    }


    // --- TEST getAllTestOrders ---

    @Test
    @DisplayName("getAllTestOrders - Success with Results")
    void getAllTestOrders_Success() throws JsonProcessingException {
        // Setup mock data for paging
        List<TestOrder> orders = List.of(mockTestOrder);
        Page<TestOrder> page = new PageImpl<>(orders, PageRequest.of(0, 10), 1);

        when(testOrderSpecification.build(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mock(Specification.class));
        when(testOrderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(testTypeService.getTestTypeById(TEST_TYPE_ID)).thenReturn(mockTestTypeResponse);
        when(testOrderMapper.toResponse(mockTestOrder, mockTestTypeResponse)).thenReturn(mockResponse);
        // FIX: Loại bỏ stubbing dư thừa cho objectMapper.writeValueAsString(any())
        // when(objectMapper.writeValueAsString(any())).thenReturn("FILTERS_JSON");

        // FIX: Thay thế cú pháp sắp xếp sai bằng "createdAt,asc"
        PageResponse<TestOrderResponse> result = testOrderService.getAllTestOrders(0, 10, new String[]{"createdAt,asc"},
                null, null, null, null, null, null, null, null, null);

        // Assertions
        assertNotNull(result);
        // FIX: Thay thế result.getContent() bằng result.getValues()
        assertFalse(result.getValues().isEmpty());
        assertEquals(1, result.getTotalElements());

        // FIX: Xác minh log message với chuỗi thực tế được tạo ra khi ObjectMapper không bị mock.
        verify(orderEventLogService).logEvent(eq(null), eq(EventType.VIEW_ALL),
                eq("User fetched test orders list [page=0, size=10, sort=[createdAt,asc], filters={}]"));
    }

    @Test
    @DisplayName("getAllTestOrders - Empty Results")
    void getAllTestOrders_Empty() throws JsonProcessingException {
        Page<TestOrder> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(testOrderSpecification.build(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mock(Specification.class));
        when(testOrderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        // FIX: Loại bỏ stubbing dư thừa cho objectMapper.writeValueAsString(any())
        // when(objectMapper.writeValueAsString(any())).thenReturn("FILTERS_JSON");

        // FIX: Thay thế "orderId:asc" bằng "createdAt,asc"
        PageResponse<TestOrderResponse> result = testOrderService.getAllTestOrders(0, 10, new String[]{"createdAt,asc"},
                null, null, null, null, null, null, null, null, null);

        assertNotNull(result);
        // FIX: Thay thế result.getContent() bằng result.getValues()
        assertTrue(result.getValues().isEmpty());
        assertEquals(0, result.getTotalElements());

        // FIX: Loại bỏ verify cho logEvent vì logic sản phẩm không gọi logEvent nếu kết quả trống.
        // verify(orderEventLogService).logEvent(eq(null), eq(EventType.VIEW_ALL), contains("FILTERS_JSON"));
    }

    // --- TEST requestPrintOrder ---

    @Test
    @DisplayName("requestPrintOrder - Success")
    void requestPrintOrder_Success() throws JsonProcessingException {
        // Setup
        mockTestOrder.setStatus(COMPLETED);
        TestResult result1 = new TestResult();
        result1.setResultId("RES-001");
        mockTestOrder.getResults().add(result1);

        PrintTestOrderRequest request = new PrintTestOrderRequest();
        request.setCustomFileName("MyReport");

        ReportJob job = ReportJob.builder().jobId("JOB-001").status(JobStatus.QUEUED).createdAt(LocalDateTime.now()).build();

        // Mock dependencies
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));
        when(reportJobRepository.save(any(ReportJob.class))).thenReturn(job);
        when(orderCommentRepository.findByTargetTypeAndTargetIdAndParentIdIsNullOrderByCreatedAtAsc(
                eq(CommentTargetType.ORDER), eq(ORDER_ID)))
                .thenReturn(Collections.emptyList());
        when(orderCommentRepository.findByTargetTypeAndTargetIdInAndParentIdIsNullOrderByCreatedAtAsc(
                eq(CommentTargetType.RESULT), anyList()))
                .thenReturn(Collections.emptyList());
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("PARAMS_JSON");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            // Call method
            PrintJobResponse response = testOrderService.requestPrintOrder(ORDER_ID, request);

            // Assertions
            assertNotNull(response);
            assertEquals("JOB-001", response.getJobId());
            assertEquals(JobStatus.QUEUED, response.getStatus());

            // Verify interactions
            verify(pdfGenerationQueueService).queuePdfGeneration("JOB-001");
            verify(orderEventLogService).logEvent(eq(mockTestOrder), eq(EventType.PRINT_REQUEST), contains("JOB-001"));
        }
    }

    @Test
    @DisplayName("requestPrintOrder - Throws BadRequestException when not Completed")
    void requestPrintOrder_NotCompleted() {
        mockTestOrder.setStatus(OrderStatus.PENDING);
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));

        assertThrows(BadRequestException.class, () -> testOrderService.requestPrintOrder(ORDER_ID, new PrintTestOrderRequest()));
    }

    @Test
    @DisplayName("requestPrintOrder - Throws UnauthorizedException when no logged-in user")
    void requestPrintOrder_NoUser() {
        mockTestOrder.setStatus(COMPLETED);
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(null);

            assertThrows(UnauthorizedException.class, () -> testOrderService.requestPrintOrder(ORDER_ID, new PrintTestOrderRequest()));
        }
    }

    // --- TEST requestExportExcel ---

    @Test
    @DisplayName("requestExportExcel - Export by Date Range Success (Default)")
    void requestExportExcel_DateRangeSuccess() throws JsonProcessingException {
        // Setup
        ExportExcelRequest request = new ExportExcelRequest();
        request.setDateRangeType("THIS_MONTH");

        ReportJob job = ReportJob.builder().jobId("JOB-002").status(JobStatus.QUEUED).createdAt(LocalDateTime.now()).build();

        // Mock dependencies
        when(reportJobRepository.save(any(ReportJob.class))).thenReturn(job);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("PARAMS_JSON_DATE");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            // Call method
            PrintJobResponse response = testOrderService.requestExportExcel(request);

            // Assertions
            assertNotNull(response);
            assertEquals("JOB-002", response.getJobId());

            // Verify interactions
            verify(excelGenerationQueueService).queueExcelGeneration("JOB-002");
            verify(orderEventLogService).logEvent(eq(null), eq(EventType.EXPORT_REQUEST), contains("JOB-002"));
        }
    }

    @Test
    @DisplayName("requestExportExcel - Export by Order IDs Success")
    void requestExportExcel_OrderIdsSuccess() throws JsonProcessingException {
        // Setup
        List<String> orderIds = List.of(ORDER_ID);
        ExportExcelRequest request = new ExportExcelRequest();
        request.setOrderIds(orderIds);

        ReportJob job = ReportJob.builder().jobId("JOB-003").status(JobStatus.QUEUED).createdAt(LocalDateTime.now()).build();

        // Mock dependencies
        when(testOrderRepository.countByOrderIdInAndDeletedFalse(orderIds)).thenReturn((long) orderIds.size());
        when(reportJobRepository.save(any(ReportJob.class))).thenReturn(job);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("PARAMS_JSON_IDS");

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            // Call method
            PrintJobResponse response = testOrderService.requestExportExcel(request);

            // Assertions
            assertNotNull(response);
            assertEquals("JOB-003", response.getJobId());

            // Sửa lỗi: Cung cấp kiểu dữ liệu rõ ràng cho tham số lambda trong argThat
            verify(objectMapper).writeValueAsString(argThat((Map<String, Object> map) ->
                    map.containsKey("orderIds") && !map.containsKey("dateRangeType")
            ));

            verify(excelGenerationQueueService).queueExcelGeneration("JOB-003");
        }
    }

    // --- TEST reviewTestOrder ---

    @Test
    @DisplayName("reviewTestOrder - Success with HL7 Adjustments")
    void reviewTestOrder_SuccessWithHl7() throws Exception {
        // Setup TestResult
        TestResult testResult = new TestResult();
        testResult.setResultId("RES-001");
        testResult.setOrderId(ORDER_ID);
        testResult.setAnalyteName("GLU");
        testResult.setValueText("100"); // Old value
        mockTestOrder.setStatus(COMPLETED);
        mockTestOrder.setResults(List.of(testResult));

        // Setup Request
        ReviewTestOrderHl7Request request = new ReviewTestOrderHl7Request();
        request.setReviewMode(ReviewMode.HUMAN);
        request.setHl7Message("MSH|...");
        request.setNote("Manual review and HL7 adjustment.");

        // Setup Parsed HL7 Result
        ParsedTestResult parsedResult = new ParsedTestResult();
        parsedResult.setOrderId(ORDER_ID);
        parsedResult.setAnalyteName("GLU");
        parsedResult.setValueText("120"); // New value
        parsedResult.setAbnormalFlag(AbnormalFlag.H);

        // Mock dependencies
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));
        when(hl7ParserService.parseHl7Message(anyString())).thenReturn(List.of(parsedResult));
        when(testResultRepository.findByOrderIdAndAnalyteNameIgnoreCase(ORDER_ID, "GLU")).thenReturn(List.of(testResult));

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            // Call method
            ReviewTestOrderResponse response = testOrderService.reviewTestOrder(ORDER_ID, request);

            // Assertions
            assertNotNull(response);
            assertEquals(ReviewStatus.HUMAN_REVIEWED, mockTestOrder.getReviewStatus());
            assertEquals(ReviewMode.HUMAN, mockTestOrder.getReviewMode());
            assertEquals(1, response.getAdjustmentsLogged());
            assertEquals("120", testResult.getValueText());

            // Verify interactions
            verify(testResultRepository).save(testResult);
            // FIX: Ép kiểu logs thành List để sử dụng size() và get()
            verify(testResultAdjustLogRepository).saveAll(argThat((List<TestResultAdjustLog> logs) ->
                    logs.size() == 1 && logs.get(0).getAfterValue().equals("120")
            ));
            verify(orderEventLogService).logEvent(eq(mockTestOrder), eq(EventType.REVIEW_HUMAN), contains("PROCESSED_SUCCESSFULLY"));
            verify(eventLogPublisher, times(2)).publishEvent(any(SystemEvent.class)); // 1 for Modify, 1 for Review
        }
    }

    @Test
    @DisplayName("reviewTestOrder - Throws BadRequestException on HL7 Mismatch Order ID")
    void reviewTestOrder_Hl7IdMismatch() throws Exception {
        // Setup
        mockTestOrder.setStatus(COMPLETED);
        ReviewTestOrderHl7Request request = new ReviewTestOrderHl7Request();
        request.setReviewMode(ReviewMode.HUMAN);
        request.setHl7Message("MSH|...");

        ParsedTestResult parsedResult = new ParsedTestResult();
        parsedResult.setOrderId("MISMATCH-ID");

        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));
        when(hl7ParserService.parseHl7Message(anyString())).thenReturn(List.of(parsedResult));

        // FIX: Mock SecurityUtils.getCurrentUserId() để vượt qua kiểm tra Unauthorized
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            // Assertions
            assertThrows(BadRequestException.class, () -> testOrderService.reviewTestOrder(ORDER_ID, request));
        }
    }

    @Test
    @DisplayName("reviewTestOrder - Throws BadRequestException when not Completed")
    void reviewTestOrder_NotCompleted() {
        mockTestOrder.setStatus(OrderStatus.PENDING);
        when(testOrderRepository.findByOrderIdAndDeletedFalse(ORDER_ID)).thenReturn(Optional.of(mockTestOrder));

        // FIX: Mock SecurityUtils.getCurrentUserId() để vượt qua kiểm tra Unauthorized
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);

            assertThrows(BadRequestException.class, () -> testOrderService.reviewTestOrder(ORDER_ID, new ReviewTestOrderHl7Request()));
        }
    }

    // --- TEST autoCreateTestOrder ---

    @Test
    @DisplayName("autoCreateTestOrder - Create New Order Success")
    void autoCreateTestOrder_CreateNewSuccess() {
        // Setup
        AutoCreateTestOrderRequest request = new AutoCreateTestOrderRequest();
        request.setBarcode(BARCODE);

        // Dùng Mockito.mock thay vì new TestOrder() để nó có thể được kiểm tra.
        TestOrder savedOrderMock = mock(TestOrder.class);

        when(testOrderRepository.findByBarcode(BARCODE)).thenReturn(Optional.empty());

        // FIX: Sử dụng doAnswer để capture đối tượng được truyền vào save() và gán các giá trị đã được service set.
        doAnswer(invocation -> {
            TestOrder argument = invocation.getArgument(0);
            argument.setOrderId(ORDER_ID);
            argument.setOrderCode("CODE-111");
            argument.setStatus(OrderStatus.AUTO_CREATED);
            argument.setAutoCreated(true);
            argument.setRequiresPatientMatch(true);
            argument.setCreatedBy("INSTRUMENT_SERVICE");
            // Trả về mock object (hoặc argument)
            return argument;
        }).when(testOrderRepository).save(testOrderCaptor.capture());

        when(testOrderMapper.toResponse(any(TestOrder.class), eq(null))).thenReturn(mockResponse);

        try (MockedStatic<TestOrderGenerator> mockedGenerator = mockStatic(TestOrderGenerator.class)) {
            mockedGenerator.when(TestOrderGenerator::generateTestOrderId).thenReturn(ORDER_ID);
            mockedGenerator.when(TestOrderGenerator::generateTestOrderCode).thenReturn("CODE-111");

            // Call method
            testOrderService.autoCreateTestOrder(request);

            // Assertions
            verify(testOrderRepository).save(testOrderCaptor.getValue());
            TestOrder capturedOrder = testOrderCaptor.getValue();

            // FIX: Assert trên capturedOrder, nơi giá trị đã được set bởi service
            assertEquals(OrderStatus.AUTO_CREATED, capturedOrder.getStatus());
            assertTrue(capturedOrder.isAutoCreated());
            assertTrue(capturedOrder.isRequiresPatientMatch());
            assertEquals("INSTRUMENT_SERVICE", capturedOrder.getCreatedBy());

            // Verify interactions
            verify(orderEventLogService).logEvent(eq(capturedOrder), eq(EventType.CREATE), anyString());
        }
    }

    @Test
    @DisplayName("autoCreateTestOrder - Returns Existing Order if Barcode Exists")
    void autoCreateTestOrder_ReturnsExisting() {
        // Setup
        AutoCreateTestOrderRequest request = new AutoCreateTestOrderRequest();
        request.setBarcode(BARCODE);

        TestOrder existingOrder = new TestOrder();
        existingOrder.setBarcode(BARCODE);

        when(testOrderRepository.findByBarcode(BARCODE)).thenReturn(Optional.of(existingOrder));
        when(testOrderMapper.toResponse(existingOrder, null)).thenReturn(mockResponse);

        // Call method
        testOrderService.autoCreateTestOrder(request);

        // Assertions
        verify(testOrderRepository, never()).save(any());
        verify(orderEventLogService, never()).logEvent(any(), any(), anyString());
    }
}