/*
 * @ {#} TestCatalog.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.mappers;

import fit.test_order_service.dtos.response.TestCatalogResponse;
import fit.test_order_service.entities.TestCatalog;
import org.springframework.stereotype.Component;

/*
 * @description: Mapper class to convert TestCatalog entity to TestCatalogResponse DTO
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Component
public class TestCatalogMapper {
    public TestCatalogResponse toResponse(TestCatalog testCatalog) {
        if (testCatalog == null) {
            return null;
        }

        return TestCatalogResponse.builder()
                .id(testCatalog.getId())
                .loincCode(testCatalog.getLoincCode())
                .localCode(testCatalog.getLocalCode())
                .testName(testCatalog.getTestName())
                .specimenType(testCatalog.getSpecimenType())
                .unit(testCatalog.getUnit())
                .referenceRange(testCatalog.getReferenceRange())
                .method(testCatalog.getMethod())
                .active(testCatalog.getActive())
                .build();
    }
}
