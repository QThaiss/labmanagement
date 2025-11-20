/*
 * @ {#} SampleAnalysisWorkflowServiceTest.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service;

import fit.instrument_service.client.TestOrderFeignClient;
import fit.instrument_service.dtos.request.InitiateWorkflowRequest;
import fit.instrument_service.dtos.request.SampleInput;
import fit.instrument_service.dtos.response.SampleResponse;
import fit.instrument_service.dtos.response.WorkflowResponse;
import fit.instrument_service.entities.BloodSample;
import fit.instrument_service.entities.Instrument;
import fit.instrument_service.entities.SampleProcessingWorkflow;
import fit.instrument_service.enums.InstrumentStatus;
import fit.instrument_service.enums.SampleStatus;
import fit.instrument_service.enums.WorkflowStatus;
import fit.instrument_service.exceptions.NotFoundException;
import fit.instrument_service.repositories.BloodSampleRepository;
import fit.instrument_service.repositories.CassetteRepository;
import fit.instrument_service.repositories.InstrumentRepository;
import fit.instrument_service.repositories.SampleProcessingWorkflowRepository;
import fit.instrument_service.services.BarcodeValidationService;
import fit.instrument_service.services.NotificationService;
import fit.instrument_service.services.ReagentCheckService;
import fit.instrument_service.services.impl.SampleAnalysisWorkflowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
class SampleAnalysisWorkflowServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private BloodSampleRepository bloodSampleRepository;

    @Mock
    private SampleProcessingWorkflowRepository workflowRepository;

    @Mock
    private CassetteRepository cassetteRepository;

    @Mock
    private BarcodeValidationService barcodeValidationService;

    @Mock
    private ReagentCheckService reagentCheckService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TestOrderFeignClient testOrderFeignClient;

    @InjectMocks
    private SampleAnalysisWorkflowServiceImpl workflowService;

    private Instrument instrument;
    private String instrumentId = "instrument-001";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        instrument = new Instrument();
        instrument.setId(instrumentId);
        instrument.setStatus(InstrumentStatus.AVAILABLE);
    }

    @Test
    void testInitiateWorkflow_Success() {
        // Arrange
        SampleInput sampleInput = new SampleInput("BARCODE001", "order-001", "cassette-001");
        InitiateWorkflowRequest request = new InitiateWorkflowRequest();
        request.setInstrumentId(instrumentId);
        request.setSamples(Arrays.asList(sampleInput));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(reagentCheckService.areReagentsSufficient(instrumentId)).thenReturn(true);
        when(barcodeValidationService.isValidBarcode(anyString())).thenReturn(true);

        SampleProcessingWorkflow workflow = new SampleProcessingWorkflow();
        workflow.setId("workflow-001");
        workflow.setStatus(WorkflowStatus.INITIATED);
        when(workflowRepository.save(any(SampleProcessingWorkflow.class))).thenReturn(workflow);

        BloodSample sample = new BloodSample();
        sample.setId("sample-001");
        sample.setBarcode("BARCODE001");
        sample.setStatus(SampleStatus.VALIDATED);
        when(bloodSampleRepository.save(any(BloodSample.class))).thenReturn(sample);
        when(bloodSampleRepository.findByWorkflowId(anyString())).thenReturn(Arrays.asList(sample));

        when(instrumentRepository.save(any(Instrument.class))).thenReturn(instrument);

        // Act
        WorkflowResponse response = workflowService.initiateWorkflow(request);

        // Assert
        assertNotNull(response);
        assertEquals("workflow-001", response.getWorkflowId());
        verify(instrumentRepository).findById(instrumentId);
        verify(reagentCheckService).areReagentsSufficient(instrumentId);
        verify(workflowRepository, atLeastOnce()).save(any(SampleProcessingWorkflow.class));
    }

    @Test
    void testInitiateWorkflow_InstrumentNotFound() {
        // Arrange
        InitiateWorkflowRequest request = new InitiateWorkflowRequest();
        request.setInstrumentId(instrumentId);
        request.setSamples(Arrays.asList(new SampleInput("BARCODE001", null, null)));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> workflowService.initiateWorkflow(request));
    }

    @Test
    void testInitiateWorkflow_InstrumentNotAvailable() {
        // Arrange
        instrument.setStatus(InstrumentStatus.RUNNING);
        InitiateWorkflowRequest request = new InitiateWorkflowRequest();
        request.setInstrumentId(instrumentId);
        request.setSamples(Arrays.asList(new SampleInput("BARCODE001", null, null)));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> workflowService.initiateWorkflow(request));
    }

    @Test
    void testInitiateWorkflow_InsufficientReagents() {
        // Arrange
        InitiateWorkflowRequest request = new InitiateWorkflowRequest();
        request.setInstrumentId(instrumentId);
        request.setSamples(Arrays.asList(new SampleInput("BARCODE001", null, null)));

        when(instrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
        when(reagentCheckService.areReagentsSufficient(instrumentId)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> workflowService.initiateWorkflow(request));
        verify(notificationService).notifyInsufficientReagents(instrumentId);
    }

    @Test
    void testGetWorkflowStatus_Success() {
        // Arrange
        String workflowId = "workflow-001";
        SampleProcessingWorkflow workflow = new SampleProcessingWorkflow();
        workflow.setId(workflowId);
        workflow.setInstrumentId(instrumentId);
        workflow.setStatus(WorkflowStatus.COMPLETED);

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // Act
        WorkflowResponse response = workflowService.getWorkflowStatus(workflowId);

        // Assert
        assertNotNull(response);
        assertEquals(workflowId, response.getWorkflowId());
        assertEquals(WorkflowStatus.COMPLETED, response.getStatus());
    }

    @Test
    void testGetWorkflowStatus_NotFound() {
        // Arrange
        String workflowId = "workflow-001";
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> workflowService.getWorkflowStatus(workflowId));
    }

    @Test
    void testGetWorkflowSamples() {
        // Arrange
        String workflowId = "workflow-001";
        BloodSample sample1 = new BloodSample();
        sample1.setId("sample-001");
        sample1.setBarcode("BARCODE001");
        sample1.setWorkflowId(workflowId);
        sample1.setStatus(SampleStatus.COMPLETED);

        BloodSample sample2 = new BloodSample();
        sample2.setId("sample-002");
        sample2.setBarcode("BARCODE002");
        sample2.setWorkflowId(workflowId);
        sample2.setStatus(SampleStatus.COMPLETED);

        when(bloodSampleRepository.findByWorkflowId(workflowId))
                .thenReturn(Arrays.asList(sample1, sample2));

        // Act
        List<SampleResponse> samples = workflowService.getWorkflowSamples(workflowId);

        // Assert
        assertNotNull(samples);
        assertEquals(2, samples.size());
        assertEquals("sample-001", samples.get(0).getSampleId());
        assertEquals("sample-002", samples.get(1).getSampleId());
    }
}
