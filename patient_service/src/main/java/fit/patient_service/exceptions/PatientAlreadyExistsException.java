/*
 * @ {#} PatientAlreadyExistsException.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.exceptions;

/*
 * @description: Exception thrown when attempting to create a patient that already exists
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
public class PatientAlreadyExistsException extends RuntimeException {
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}
