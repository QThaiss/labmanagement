
package fit.test_order_service.exceptions;

/*
 * @description: Custom exception for not found resources
 * @author: Tran Hien Vinh
 * @date:   13/10/2025
 * @version:    1.0
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
