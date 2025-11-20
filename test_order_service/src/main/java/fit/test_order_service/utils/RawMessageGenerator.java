/*
 * @ {#} TestResultGenerator.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

/*
 * @description: Utility class for generating unique Raw Message IDs
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@UtilityClass
public class RawMessageGenerator {
    private static final Random RANDOM = new Random();

    public String generateRawMessageId() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int number = RANDOM.nextInt(9000) + 1000; // 1000-9999
        return String.format("RMI-%s-%s-%d", datePart, uuidSuffix, number);
    }
}
