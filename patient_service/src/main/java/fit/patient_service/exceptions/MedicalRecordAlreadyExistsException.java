package fit.patient_service.exceptions;

public class MedicalRecordAlreadyExistsException extends RuntimeException {
    public MedicalRecordAlreadyExistsException(String message) {
        super(message);
    }
}
