/*
 * @ {#} ParameterRangeServiceImpl.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import fit.warehouse_service.dtos.request.CreateParameterRangeRequest;
import fit.warehouse_service.dtos.request.UpdateParameterRangeRequest;
import fit.warehouse_service.dtos.response.ParameterRangeResponse;
import fit.warehouse_service.entities.ParameterRange;
import fit.warehouse_service.entities.TestParameter;
import fit.warehouse_service.exceptions.AlreadyExistsException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.mappers.ParameterRangeMapper;
import fit.warehouse_service.repositories.ParameterRangeRepository;
import fit.warehouse_service.repositories.TestParameterRepository;
import fit.warehouse_service.services.ParameterRangeService;
import fit.warehouse_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 * @description: Service interface for managing ParameterRange entities
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterRangeServiceImpl implements ParameterRangeService {
    private final ParameterRangeRepository parameterRangeRepository;

    private final TestParameterRepository testParameterRepository;

    private final ParameterRangeMapper parameterRangeMapper;

    @Override
    public ParameterRangeResponse createParameterRange(CreateParameterRangeRequest request) {
        log.info("Creating parameter range for test parameter abbreviation : {}",
                request.getAbbreviation());

        // Validate min/max values
        if (request.getMinValue() >= request.getMaxValue()) {
            throw new IllegalArgumentException("Minimum value must be less than maximum value");
        }

        // Check if test parameter exists
        TestParameter testParameter = testParameterRepository.findByAbbreviation(request.getAbbreviation().toUpperCase())
                .orElseThrow(() -> new NotFoundException("Test parameter not found with abbreviation: " + request.getAbbreviation()));

        // Check for duplicate gender range for the same test parameter
        boolean existsForGender = parameterRangeRepository.existsByTestParameterIdAndGender(
                testParameter.generateId(), request.getGender());

        if (existsForGender) {
            throw new IllegalArgumentException("Parameter range already exists for gender: " + request.getGender() +
                    " in test parameter: " + testParameter.getParamName());
        }

        // Create parameter range
        ParameterRange parameterRange = new ParameterRange();
        parameterRange.setGender(request.getGender());
        parameterRange.setMinValue(request.getMinValue());
        parameterRange.setMaxValue(request.getMaxValue());
        parameterRange.setUnit(request.getUnit());
        parameterRange.setTestParameter(testParameter);
        parameterRange.setCreatedByUserId(SecurityUtils.getCurrentUserId());
        parameterRange.setCreatedAt(LocalDateTime.now());

        ParameterRange savedParameterRange = parameterRangeRepository.save(parameterRange);

        log.info("Parameter range created successfully with ID: {}", savedParameterRange.getId());

        return parameterRangeMapper.mapToResponse(savedParameterRange);
    }

    @Override
    @Transactional
    public ParameterRangeResponse updateParameterRange(String parameterRangeId, UpdateParameterRangeRequest request) {
        log.info("Updating parameter range with ID: {}", parameterRangeId);

        // Tìm kiếm ParameterRange hiện có
        ParameterRange existingParameterRange = parameterRangeRepository.findById(parameterRangeId)
                .orElseThrow(() -> new NotFoundException("Parameter range not found with ID: " + parameterRangeId));

        // Validate giá trị min/max nếu cả hai đều được cung cấp
        if (request.getMinValue() != null && request.getMaxValue() != null) {
            if (request.getMinValue() >= request.getMaxValue()) {
                throw new IllegalArgumentException("Minimum value must be less than maximum value");
            }
        } else if (request.getMinValue() != null) {
            if (request.getMinValue() >= existingParameterRange.getMaxValue()) {
                throw new IllegalArgumentException("Minimum value must be less than current maximum value");
            }
        } else if (request.getMaxValue() != null) {
            if (existingParameterRange.getMinValue() >= request.getMaxValue()) {
                throw new IllegalArgumentException("Maximum value must be greater than current minimum value");
            }
        }

        // Kiểm tra xem giới tính có trùng lặp không nếu giới tính đang được cập nhật
        if (request.getGender() != null && !request.getGender().equals(existingParameterRange.getGender())) {
            boolean existsForGender = parameterRangeRepository.existsByTestParameterIdAndGender(
                    existingParameterRange.getTestParameter().getId(), request.getGender());

            if (existsForGender) {
                throw new IllegalArgumentException("Parameter range already exists for gender: " + request.getGender() +
                        " in test parameter: " + existingParameterRange.getTestParameter().getParamName());
            }
        }

        // Áp dụng các cập nhật từ request
        if (request.getGender() != null) {
            existingParameterRange.setGender(request.getGender());
        }

        if (request.getMinValue() != null) {
            existingParameterRange.setMinValue(request.getMinValue());
        }

        if (request.getMaxValue() != null) {
            existingParameterRange.setMaxValue(request.getMaxValue());
        }

        if (request.getUnit() != null) {
            existingParameterRange.setUnit(request.getUnit().trim());
        }

        existingParameterRange.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        existingParameterRange.setUpdatedAt(LocalDateTime.now());

        ParameterRange updatedParameterRange = parameterRangeRepository.save(existingParameterRange);

        log.info("Parameter range updated successfully with ID: {}", updatedParameterRange.getId());

        return parameterRangeMapper.mapToResponse(updatedParameterRange);
    }

    @Override
    @Transactional
    public void deleteParameterRange(String parameterRangeId) {
        log.info("Deleting parameter range with ID: {}", parameterRangeId);

        // Tìm kiếm ParameterRange hiện có
        ParameterRange existingParameterRange = parameterRangeRepository.findById(parameterRangeId)
                .orElseThrow(() -> new NotFoundException("Parameter range not found with ID: " + parameterRangeId));

        // Đánh dấu là đã xóa mềm
        existingParameterRange.setDeleted(true);
        existingParameterRange.setDeletedAt(LocalDateTime.now());
        existingParameterRange.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        existingParameterRange.setUpdatedAt(LocalDateTime.now());

        parameterRangeRepository.save(existingParameterRange);

        log.info("Parameter range deleted successfully with ID: {}", parameterRangeId);
    }

    @Override
    @Transactional
    public ParameterRangeResponse restoreParameterRange(String parameterRangeId) {
        log.info("Restoring parameter range with ID: {}", parameterRangeId);

        // Tìm kiếm ParameterRange đã xóa mềm
        ParameterRange deletedParameterRange = parameterRangeRepository.findByIdIncludingDeleted(parameterRangeId)
                .orElseThrow(() -> new NotFoundException("Parameter range not found with ID: " + parameterRangeId));

        // Kiểm tra xem ParameterRange có thực sự bị xóa không
        if (!deletedParameterRange.isDeleted()) {
            throw new IllegalArgumentException("Parameter range with ID: " + parameterRangeId + " is not deleted");
        }

        // Kiểm tra trùng lặp giới tính trong cùng TestParameter
        boolean existsForGender = parameterRangeRepository.existsByTestParameterIdAndGenderAndDeletedFalse(
                deletedParameterRange.getTestParameter().getId(), deletedParameterRange.getGender());

        if (existsForGender) {
            throw new AlreadyExistsException("Cannot restore: Parameter range already exists for gender '"
                    + deletedParameterRange.getGender() + "' in test parameter: "
                    + deletedParameterRange.getTestParameter().getParamName());
        }

        // Khôi phục ParameterRange
        deletedParameterRange.setDeleted(false);
        deletedParameterRange.setDeletedAt(null);
        deletedParameterRange.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        deletedParameterRange.setUpdatedAt(LocalDateTime.now());

        ParameterRange restoredParameterRange = parameterRangeRepository.save(deletedParameterRange);

        log.info("Parameter range restored successfully with ID: {}", restoredParameterRange.getId());

        return parameterRangeMapper.mapToResponse(restoredParameterRange);
    }

    @Override
    @Transactional(readOnly = true)
    public ParameterRangeResponse getParameterRangeById(String parameterRangeId) {
        log.info("Getting parameter range with ID: {}", parameterRangeId);

        ParameterRange parameterRange = parameterRangeRepository.findById(parameterRangeId)
                .orElseThrow(() -> new NotFoundException("Parameter range not found with ID: " + parameterRangeId));

        log.info("Parameter range retrieved successfully with ID: {}", parameterRangeId);

        return parameterRangeMapper.mapToResponse(parameterRange);
    }
}
