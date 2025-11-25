/*
 * @ (#) TestResultBackupService.java    1.0    24/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.event.TestResultPublishedEvent;

public interface TestResultBackupService {
    void backupTestResult(TestResultPublishedEvent event);
}
