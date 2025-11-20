/*
 * @ {#} ResultIngestGenerator.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/*
 * @description: Utility class for generating unique Result Ingest IDs
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@UtilityClass
public class ResultIngestGenerator {
    public String generateResultIngestId() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("yyMMdd"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return String.format("RSII-%s-%s", datePart, uuidSuffix);
    }
}
