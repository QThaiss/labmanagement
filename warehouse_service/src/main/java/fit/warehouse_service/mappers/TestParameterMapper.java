/*
 * @ {#} TestParameterMapper.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.response.ParameterRangeResponse;
import fit.warehouse_service.dtos.response.TestParameterResponse;
import fit.warehouse_service.entities.ParameterRange;
import fit.warehouse_service.entities.TestParameter;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * @description: Mapper class for TestParameter entity
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Component
public class TestParameterMapper {
    public TestParameterResponse mapToResponse(TestParameter testParameter) {
        return TestParameterResponse.builder()
                .testParameterId(testParameter.getId())
                .paramName(testParameter.getParamName())
                .abbreviation(testParameter.getAbbreviation())
                .description(testParameter.getDescription())
                .createdAt(testParameter.getCreatedAt())
                .createdByUserId(testParameter.getCreatedByUserId())
                .updatedAt(testParameter.getUpdatedAt())
                .updatedByUserId(testParameter.getUpdatedByUserId())
                .deletedAt(testParameter.getDeletedAt())
                .isDeleted(testParameter.isDeleted())
                .parameterRanges(
                        testParameter.getParameterRanges() == null
                                ? List.of() // trả về rỗng
                                : testParameter.getParameterRanges().stream()
                                .map(this::mapRangesToResponses)
                                .toList()
                )
                .build();
    }

    private ParameterRangeResponse mapRangesToResponses(ParameterRange range) {
        return ParameterRangeResponse.builder()
                .parameterRangeId(range.getId())
                .gender(range.getGender())
                .minValue(range.getMinValue())
                .maxValue(range.getMaxValue())
                .unit(range.getUnit())
                .createdByUserId(range.getCreatedByUserId())
                .createdAt(range.getCreatedAt())
                .updatedByUserId(range.getUpdatedByUserId())
                .updatedAt(range.getUpdatedAt())
                .deletedAt(range.getDeletedAt())
                .isDeleted(range.isDeleted())
                .build();
    }
}
