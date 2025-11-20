/*
 * @ {#} BarcodeValidationServiceTest.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service;

import fit.instrument_service.services.BarcodeValidationService;
import fit.instrument_service.services.impl.BarcodeValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
class BarcodeValidationServiceTest {

    private BarcodeValidationService barcodeValidationService;

    @BeforeEach
    void setUp() {
        barcodeValidationService = new BarcodeValidationServiceImpl();
    }

    @Test
    void testValidBarcode() {
        assertTrue(barcodeValidationService.isValidBarcode("ABC12345"));
        assertTrue(barcodeValidationService.isValidBarcode("TEST-001"));
        assertTrue(barcodeValidationService.isValidBarcode("SAMPLE_123"));
        assertTrue(barcodeValidationService.isValidBarcode("12345678"));
    }

    @Test
    void testInvalidBarcode_Null() {
        assertFalse(barcodeValidationService.isValidBarcode(null));
    }

    @Test
    void testInvalidBarcode_Empty() {
        assertFalse(barcodeValidationService.isValidBarcode(""));
        assertFalse(barcodeValidationService.isValidBarcode("   "));
    }

    @Test
    void testInvalidBarcode_TooShort() {
        assertFalse(barcodeValidationService.isValidBarcode("ABC123"));
    }

    @Test
    void testInvalidBarcode_TooLong() {
        assertFalse(barcodeValidationService.isValidBarcode("ABCDEFGHIJ1234567890X"));
    }

    @Test
    void testInvalidBarcode_InvalidCharacters() {
        assertFalse(barcodeValidationService.isValidBarcode("ABC@12345"));
        assertFalse(barcodeValidationService.isValidBarcode("TEST 001"));
        assertFalse(barcodeValidationService.isValidBarcode("SAMPLE#123"));
    }
}
