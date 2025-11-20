/*
 * @ {#} TestValidator.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.validators;

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.repositories.TestOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*
 * @description: Validator for TestOrder entity
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class TestOrderValidator {
    private final TestOrderRepository testOrderRepository;

    public TestOrder validateForUpdate(String orderCode) {
        TestOrder testOrder = testOrderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Test order not found with code: " + orderCode));

        if (testOrder.getDeletedAt() != null) {
            throw new NotFoundException("Cannot update deleted test order");
        }

        return testOrder;
    }
}
