/*
 * @ {#} Hl7ProcessingService.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.dtos.request.Hl7MessageRequest;
import fit.test_order_service.dtos.response.Hl7ProcessResponse;

/*
 * @description: Service interface for processing HL7 messages.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
public interface Hl7ProcessingService {
    /**
     * Xử lý tin nhắn HL7 và trả về phản hồi.
     *
     * @param request yêu cầu chứa tin nhắn HL7 cần xử lý
     * @return phản hồi sau khi xử lý tin nhắn HL7
     */
    Hl7ProcessResponse processHl7Message(Hl7MessageRequest request);
}
