/*
 * @ {#} ParameterRangeMapper.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.mappers;

import fit.warehouse_service.dtos.response.ParameterRangeResponse;
import fit.warehouse_service.entities.ParameterRange;
import org.springframework.stereotype.Component;


/*
 * @description: Mapper class for ParameterRange entity
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Component
public class ParameterRangeMapper {
    public ParameterRangeResponse mapToResponse(ParameterRange parameterRange) {
        return ParameterRangeResponse.builder()
                .parameterRangeId(parameterRange.getId())
                .gender(parameterRange.getGender())
                .minValue(parameterRange.getMinValue())
                .maxValue(parameterRange.getMaxValue())
                .unit(parameterRange.getUnit())
                .testParameterId(parameterRange.getTestParameter().getId())
                .createdAt(parameterRange.getCreatedAt())
                .createdByUserId(parameterRange.getCreatedByUserId())
                .updatedAt(parameterRange.getUpdatedAt())
                .updatedByUserId(parameterRange.getUpdatedByUserId())
                .deletedAt(parameterRange.getDeletedAt())
                .isDeleted(parameterRange.isDeleted())
                .build();
    }
}
