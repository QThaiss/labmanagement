/*
 * @ {#} PatientAlreadyExistsException.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Exception thrown when a patient's Gender is invalid.
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
public class MedicalRecordStatusException extends RuntimeException {
    public MedicalRecordStatusException(String message) {
        super(message);
    }
}
