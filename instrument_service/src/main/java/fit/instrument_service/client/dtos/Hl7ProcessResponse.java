/*
 * @ {#} Hl7ProcessResponse.java   1.0     21/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.client.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   21/11/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hl7ProcessResponse {
    private String messageId;
    private String status;
    private String rawId;
    private List<String> resultIds;
    private String quarantineId;
    private String errorMessage;
    private LocalDateTime processedAt;
}
