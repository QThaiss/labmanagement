/*
 * @ {#} AccessLogGenerator.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.utils;

import fit.patient_service.enums.AccessAction;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/*
 * @description: Utility class for generating access log IDs
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@UtilityClass
public class AccessLogGenerator {
    private static final String PREFIX = "LOG";
    private static final int SHORT_ID_MAX = 16;
    private static final int RANDOM_LEN = 6;
    private static final SecureRandom RNG = new SecureRandom();
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(VN_ZONE);

    public String generateAccessLogId(String entityType, String entityId, AccessAction action) {
        String ts = TS_FMT.format(Instant.now());
        String et = sanitize(entityType);
        String act = action != null ? action.name() : "UNKNOWN";
        String shortEntityId = shorten(entityId);
        String rand = randomBase36(RANDOM_LEN);
        // LOG_20250924HHMMSS_MEDICAL_RECORD_CREATE_MR17588095_A1B2C3
        return String.join("_", PREFIX, ts, et, act, shortEntityId, rand);
    }

    private String sanitize(String s) {
        if (s == null || s.isBlank()) return "UNKNOWN";
        return s.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private String shorten(String entityId) {
        if (entityId == null || entityId.isBlank()) return "NA";
        String trimmed = entityId.trim();
        return trimmed.length() > SHORT_ID_MAX ? trimmed.substring(0, SHORT_ID_MAX) : trimmed;
    }

    private String randomBase36(int len) {
        StringBuilder sb = new StringBuilder(len);
        while (sb.length() < len) {
            int v = RNG.nextInt(36);
            char c = (char) (v < 10 ? ('0' + v) : ('A' + (v - 10)));
            sb.append(c);
        }
        return sb.toString();
    }
}
