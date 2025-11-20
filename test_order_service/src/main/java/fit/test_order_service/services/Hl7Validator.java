/*
 * @ {#} Hl7Validator.java   1.0     23/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import fit.test_order_service.dtos.response.Hl7ValidationResult;

/*
 * @description: Service interface for validating HL7 message structures.
 * @author: Tran Hien Vinh
 * @date:   23/10/2025
 * @version:    1.0
 */
public interface Hl7Validator {
    /**
     * Xử lý xác thực cấu trúc tin nhắn HL7 ORU_R01.
     *
     * @param oruMessage Tin nhắn HL7 ORU_R01 để xác thực
     * @param orderId    Mã TestOrder liên quan đến tin nhắn
     * @return Kết quả xác thực HL7
     */
    Hl7ValidationResult validateHl7Structure(ORU_R01 oruMessage, String orderId);

    /**
     * Kiểm tra xem trạng thái kết quả có phải là kết quả cuối cùng không.
     *
     * @param resultStatus Trạng thái kết quả cần kiểm tra
     * @return true nếu là kết quả cuối cùng, false nếu không phải
     */
    boolean isFinalResult(String resultStatus);
}
