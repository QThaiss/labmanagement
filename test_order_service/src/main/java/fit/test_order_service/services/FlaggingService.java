/*
 * @ {#} FlaggingService.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.entities.TestResult;

/*
 * @description: Service interface for applying flagging rules to test results.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
public interface FlaggingService {
    /**
     * Applies flagging rules to the given test result.
     *
     * @param result the test result to which flagging rules will be applied
     */
    void applyFlaggingRules(TestResult result);
}
