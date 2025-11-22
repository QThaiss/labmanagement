/*
 * @ (#) TestResultResponse.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 13/10/2025
 * @version: 1.0
 */

import fit.test_order_service.enums.AbnormalFlag;
import fit.test_order_service.enums.EntrySource;
import fit.test_order_service.enums.FlagSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultResponse {
    private String id;
    private String analyteName;
    private String testName;
    private String testCode;
    private String value;
    private String unit;
    private String referenceRange;
    private AbnormalFlag abnormalFlag;
    private LocalDateTime measuredAt;
    private EntrySource entrySource;
    private String enteredBy;
    private LocalDateTime enteredAt;
//    private String flagCode;
//    private FlagSeverity flagSeverity;
}
