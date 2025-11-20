/*
 * @ (#) IdGenerator.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.utils;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class IdGenerator {

    private static final Random RANDOM = new Random();
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    /**
     * Sinh ID tùy chỉnh dựa trên tiền tố.
     * Định dạng: [PREFIX]-[Timestamp]-[UUID(8)]-[Rand(4)]
     */
    public static String generate(String prefix) {
        String datePart = LocalDateTime.now(ZONE_ID).format(DATE_FORMATTER);
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int number = RANDOM.nextInt(9000) + 1000; // 1000-9999

        return String.format("%s-%s-%s-%d", prefix, datePart, uuidSuffix, number);
    }
}
