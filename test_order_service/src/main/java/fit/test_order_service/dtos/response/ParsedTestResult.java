/*
 * @ {#} ParsedTestResult.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import fit.test_order_service.enums.AbnormalFlag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * @description: DTO for parsed test result from HL7 message
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedTestResult {
    private String orderId;
    private String testCode;
    private String analyteName;
    private String valueText;
    private String unit;
    private String referenceRange;
    private AbnormalFlag abnormalFlag;
    private LocalDateTime measuredAt;
    private String sourceMsgId;
}
