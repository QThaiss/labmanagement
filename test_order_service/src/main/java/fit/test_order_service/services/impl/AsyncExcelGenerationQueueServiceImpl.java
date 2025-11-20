/*
 * @ (#) AsyncExcelGenerationQueueServiceImpl.java    1.0    23/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 23/10/2025
 * @version: 1.0
 */

import fit.test_order_service.services.ExcelGenerationQueueService;
import fit.test_order_service.services.ExcelGenerationWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncExcelGenerationQueueServiceImpl implements ExcelGenerationQueueService {

    private final ExcelGenerationWorker excelGenerationWorker;

    @Override
    public void queueExcelGeneration(String jobId) {
        excelGenerationWorker.generateExcel(jobId); // Gọi phương thức @Async trong Worker mới
    }
}
