package fit.patient_service.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class MedicalRecordGenerator {  private static final Random RANDOM = new Random();
    public String generateMedicalRecordId() {
        long timestamp = System.currentTimeMillis();
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        int number = RANDOM.nextInt(9000) + 1000; // 1000-9999
        return String.format("MR%d%s%d", timestamp, uuidSuffix, number);
    }

    public String generateMedicalRecordCode() {
        String datePart = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String uuidSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        int number = ThreadLocalRandom.current().nextInt(100, 999);
        return String.format("MRC-%s-%s-%d", datePart, uuidSuffix, number);
    }
}
