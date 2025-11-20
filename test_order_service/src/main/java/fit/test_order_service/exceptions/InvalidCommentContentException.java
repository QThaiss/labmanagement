package fit.test_order_service.exceptions;

public class InvalidCommentContentException extends RuntimeException {
    public InvalidCommentContentException(String message) {
        super(message);
    }
}
