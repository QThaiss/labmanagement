/*
 * @ {#} ReagentCheckServiceTest.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service;

import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.ReagentStatus;
import fit.instrument_service.repositories.InstrumentReagentRepository;
import fit.instrument_service.services.impl.ReagentCheckServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
class ReagentCheckServiceTest {

    @Mock
    private InstrumentReagentRepository instrumentReagentRepository;

    @InjectMocks
    private ReagentCheckServiceImpl reagentCheckService;

    private String instrumentId = "instrument-001";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReagentsSufficient() {
        // Arrange
        InstrumentReagent reagent1 = new InstrumentReagent();
        reagent1.setReagentName("Reagent A");
        reagent1.setQuantity(50);
        reagent1.setExpirationDate(LocalDate.now().plusMonths(3));
        reagent1.setStatus(ReagentStatus.IN_USE);

        InstrumentReagent reagent2 = new InstrumentReagent();
        reagent2.setReagentName("Reagent B");
        reagent2.setQuantity(30);
        reagent2.setExpirationDate(LocalDate.now().plusMonths(6));
        reagent2.setStatus(ReagentStatus.IN_USE);

        List<InstrumentReagent> reagents = Arrays.asList(reagent1, reagent2);
        when(instrumentReagentRepository.findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE))
                .thenReturn(reagents);

        // Act
        boolean result = reagentCheckService.areReagentsSufficient(instrumentId);

        // Assert
        assertTrue(result);
        verify(instrumentReagentRepository).findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE);
    }

    @Test
    void testReagentsInsufficient_LowQuantity() {
        // Arrange
        InstrumentReagent reagent = new InstrumentReagent();
        reagent.setReagentName("Reagent A");
        reagent.setQuantity(5); // Below minimum threshold
        reagent.setExpirationDate(LocalDate.now().plusMonths(3));
        reagent.setStatus(ReagentStatus.IN_USE);

        when(instrumentReagentRepository.findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE))
                .thenReturn(Collections.singletonList(reagent));

        // Act
        boolean result = reagentCheckService.areReagentsSufficient(instrumentId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testReagentsInsufficient_Expired() {
        // Arrange
        InstrumentReagent reagent = new InstrumentReagent();
        reagent.setReagentName("Reagent A");
        reagent.setQuantity(50);
        reagent.setExpirationDate(LocalDate.now().minusDays(1)); // Expired
        reagent.setStatus(ReagentStatus.IN_USE);

        when(instrumentReagentRepository.findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE))
                .thenReturn(Collections.singletonList(reagent));

        // Act
        boolean result = reagentCheckService.areReagentsSufficient(instrumentId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testReagentsInsufficient_NoReagents() {
        // Arrange
        when(instrumentReagentRepository.findByInstrumentIdAndStatus(instrumentId, ReagentStatus.IN_USE))
                .thenReturn(Collections.emptyList());

        // Act
        boolean result = reagentCheckService.areReagentsSufficient(instrumentId);

        // Assert
        assertFalse(result);
    }
}
