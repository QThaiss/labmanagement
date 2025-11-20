/*
 * @ {#} NotFoundException.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Custom exception for not found resources
 * @author: Tran Hien Vinh
 * @date:   27/09/2025
 * @version:    1.0
 */
public class NotFoundException extends RuntimeException {
    public static final String PATIENT_MEDICAL_RECORD_NOT_FOUND = "Patient medical record not found with ID: ";

    public NotFoundException(String message) {
        super(message);
    }
}
