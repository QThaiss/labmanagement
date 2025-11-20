package fit.test_order_service.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class TestOrderGenerator {
    private static final Random RANDOM = new Random();

    public String generateTestOrderId() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int number = RANDOM.nextInt(9000) + 1000; // 1000-9999
        return String.format("TO-%s-%s-%d", datePart, uuidSuffix, number);
    }

    public String generateTestOrderCode() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        int number = ThreadLocalRandom.current().nextInt(100, 999);
        return String.format("TOC-%s-%s-%d", datePart, uuidSuffix, number);
    }

    public String generateTestItemId() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMdd"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return String.format("TII-%s-%s", datePart, uuidSuffix);
    }

    public String generateTestItemCode() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMdd"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return String.format("TIC-%s-%s", datePart, uuidSuffix);
    }

    public String generateBarcode() {
        String datePart = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMdd"));

        String randomCode = generateRandomAlphaNumeric(6);

        return String.format("LAB-BS-%s-%s", datePart, randomCode);
    }

    private String generateRandomAlphaNumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String generateTestTypeId() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int number = RANDOM.nextInt(9000) + 1000; // 1000-9999
        return String.format("TTID-%s-%s-%d", datePart, uuidSuffix, number);
    }
}
