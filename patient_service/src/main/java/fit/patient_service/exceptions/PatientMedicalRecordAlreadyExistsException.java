package fit.patient_service.exceptions;

public class PatientMedicalRecordAlreadyExistsException extends RuntimeException {
    public PatientMedicalRecordAlreadyExistsException(String message) {
        super(message);
    }
}
