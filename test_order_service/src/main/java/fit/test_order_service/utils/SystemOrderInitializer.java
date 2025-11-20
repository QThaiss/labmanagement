/*
 * @ {#} SystemOrderInitializer.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.Gender;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.enums.ReviewStatus;
import fit.test_order_service.repositories.TestOrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/*
 * @description: Initializes a system-generated TestOrder if it doesn't already exist in the database
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemOrderInitializer {

    private final TestOrderRepository testOrderRepository;

    @PostConstruct
    public void ensureSystemOrderExists() {
        String systemOrderId = "SYSTEM_ORDER_ID";

        // Check if SYSTEM_ORDER already exists
        boolean exists = testOrderRepository.existsById(systemOrderId);

        if (!exists) {
            TestOrder system = new TestOrder();
            system.setOrderId(systemOrderId);
            system.setOrderCode("SYSTEM_ORDER");
            system.setMedicalRecordId("SYSTEM");
            system.setMedicalRecordCode("SYSTEM_MRN");
            system.setFullName("SYSTEM INTERNAL");
            system.setDateOfBirth(LocalDate.of(2000, 1, 1));
            system.setAgeYearsSnapshot(0);
            system.setGender(Gender.MALE);
            system.setPhone("0000000000");
            system.setEmail("system@internal.local");
            system.setAddress("Internal system order for logging");
            system.setStatus(OrderStatus.PENDING);
            system.setReviewStatus(ReviewStatus.NONE);
            system.setCreatedBy("SYSTEM");
            system.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

            testOrderRepository.save(system);
            log.info("SYSTEM_ORDER created successfully in DB");
        } else {
            log.info("ℹSYSTEM_ORDER already exists");
        }
    }
}
