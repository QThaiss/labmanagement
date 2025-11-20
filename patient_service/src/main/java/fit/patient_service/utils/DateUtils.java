/*
 * @ {#} DateUtils.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@UtilityClass
public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ZoneOffset UTC_OFFSET = ZoneOffset.UTC;

    // Convert String to LocalDateTime at start of day
    public LocalDateTime convertStringToLocalDateTime(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Expected format: yyyy-MM-dd");
        }
    }

    /** Lấy thời gian hiện tại theo múi giờ Việt Nam */
    public static LocalDateTime nowVietnam() {
        return LocalDateTime.now(VIETNAM_ZONE);
    }

    /** Lấy thời gian hiện tại theo UTC */
    public static LocalDateTime nowUTC() {
        return LocalDateTime.now(UTC_OFFSET);
    }

    /** Chuyển từ UTC sang giờ Việt Nam */
    public static LocalDateTime toVietnamTime(LocalDateTime utcTime) {
        if (utcTime == null) return null;
        return utcTime.atOffset(UTC_OFFSET)
                .atZoneSameInstant(VIETNAM_ZONE)
                .toLocalDateTime();
    }

    /** Chuyển từ giờ Việt Nam sang UTC */
    public static LocalDateTime toUTC(LocalDateTime vnTime) {
        if (vnTime == null) return null;
        return vnTime.atZone(VIETNAM_ZONE)
                .withZoneSameInstant(UTC_OFFSET)
                .toLocalDateTime();
    }
}
