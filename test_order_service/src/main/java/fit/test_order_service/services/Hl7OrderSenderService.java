/*
 * @ {#} Hl7OrderSenderService.java   1.0     04/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.dtos.response.Hl7ProcessResponse;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;

import java.util.List;

/*
 * @description: Service interface for sending HL7 orders to instruments
 * @author: Tran Hien Vinh
 * @date:   04/11/2025
 * @version:    1.0
 */

public interface Hl7OrderSenderService {
    /**
     * Gửi order đến máy phân tích và xử lý kết quả trả về.
     *
     * @param testOrderId ID của order cần gửi
     *
     * @return kết quả xử lý HL7
     */
     Hl7ProcessResponse sendOrderAndProcessResult(String testOrderId);

     /**
      * Gửi order đến máy phân tích và nhận về HL7 response dưới dạng chuỗi.
      *
      * @param order đối tượng TestOrder cần gửi
      * @param testResults danh sách các test result
      *
      * @return chuỗi HL7 response từ máy phân tích
      */
     String sendOrderToInstrument(TestOrder order, List<TestResult> testResults);
}
