/*
 * @ (#) PdfGenerationQueueService.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

public interface PdfGenerationQueueService {
    void queuePdfGeneration(String jobId);
}
