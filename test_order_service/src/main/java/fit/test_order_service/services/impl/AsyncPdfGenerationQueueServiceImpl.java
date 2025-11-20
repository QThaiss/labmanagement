/*
 * @ (#) AsyncPdfGenerationQueueServiceImpl.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services.impl;/*
 * @description:
 * @author: Bao Thong
 * @date: 22/10/2025
 * @version: 1.0
 */

import fit.test_order_service.services.PdfGenerationQueueService;
import fit.test_order_service.services.PdfGenerationWorker; // Worker sẽ chứa logic @Async
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncPdfGenerationQueueServiceImpl implements PdfGenerationQueueService {

    private final PdfGenerationWorker pdfGenerationWorker;

    @Override
    public void queuePdfGeneration(String jobId) {
        // Gọi trực tiếp phương thức @Async trong Worker
        pdfGenerationWorker.generatePdf(jobId);
    }
}
