package fit.test_order_service.exceptions;

public class DuplicateCommentException extends RuntimeException {
    public DuplicateCommentException(String message) {
        super(message);
    }
}
