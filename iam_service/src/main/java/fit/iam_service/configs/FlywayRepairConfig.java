/*
 * @ (#) FlywayRepairConfig.java    1.0    02/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 02/10/2025
 * @version: 1.0
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

@Configuration
public class FlywayRepairConfig {
    @Bean
    public FlywayMigrationStrategy flywayRepairThenMigrate() {
        return flyway -> {
            flyway.repair();   // xoá dấu vết migration fail / sync checksum
            flyway.migrate();  // chạy lại các migration
        };
    }
}