/*
 * @ {#} Hl7ParserService.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.dtos.response.Hl7Metadata;
import fit.test_order_service.dtos.response.ParsedTestResult;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;

import java.util.List;

/*
 * @description: Service interface for parsing HL7 messages.
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
public interface Hl7ParserService {
    /**
     * Phân tích tin nhắn HL7 và trích xuất kết quả xét nghiệm.
     *
     * @param hl7Payload nội dung tin nhắn HL7 cần phân tích
     * @return a list of parsed test results
     */
    List<ParsedTestResult> parseHl7Message(String hl7Payload);

    /**
     * Trích xuất metadata từ tin nhắn HL7.
     *
     * @param hl7Payload nội dung tin nhắn HL7
     * @return metadata của tin nhắn HL7
     */
    Hl7Metadata extractMetadata(String hl7Payload);

    /**
     * Xây dựng tin nhắn HL7 từ order và các item liên quan.
     *
     * @param testOrder đối tượng TestOrder
     * @param testResults danh sách các TestResult liên quan đến order
     * @return chuỗi tin nhắn HL7 được xây dựng
     */
    String buildHl7OrderMessage(TestOrder testOrder, List<TestResult> testResults);
}
