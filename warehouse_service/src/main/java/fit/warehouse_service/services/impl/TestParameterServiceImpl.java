/*
 * @ {#} TestParameterServiceImpl.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services.impl;

import fit.warehouse_service.constants.SortFields;
import fit.warehouse_service.dtos.request.CreateTestParameterRequest;
import fit.warehouse_service.dtos.request.UpdateTestParameterRequest;
import fit.warehouse_service.dtos.response.FilterInfo;
import fit.warehouse_service.dtos.response.PageResponse;
import fit.warehouse_service.dtos.response.TestParameterResponse;
import fit.warehouse_service.entities.TestParameter;
import fit.warehouse_service.exceptions.AlreadyExistsException;
import fit.warehouse_service.exceptions.NotFoundException;
import fit.warehouse_service.mappers.TestParameterMapper;
import fit.warehouse_service.repositories.TestParameterRepository;
import fit.warehouse_service.services.TestParameterService;
import fit.warehouse_service.specifications.TestParameterSpecification;
import fit.warehouse_service.utils.SecurityUtils;
import fit.warehouse_service.utils.SortUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Service implementation for managing TestParameter entities
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestParameterServiceImpl implements TestParameterService {
    private final TestParameterRepository testParameterRepository;

    private final TestParameterSpecification testParameterSpecification;

    private final TestParameterMapper testParameterMapper;

    @Override
    @Transactional
    public TestParameterResponse createTestParameter(CreateTestParameterRequest request) {
        log.info("Creating test parameter: {}", request.getParamName());

        if (testParameterRepository.existsByParamName(request.getParamName())) {
            throw new AlreadyExistsException("Test parameter with name '" + request.getParamName() + "' already exists");
        }

        if (testParameterRepository.existsByAbbreviation(request.getAbbreviation())) {
            throw new AlreadyExistsException("Test parameter with abbreviation '" + request.getAbbreviation() + "' already exists");
        }

        TestParameter testParameter = new TestParameter();
        testParameter.setParamName(request.getParamName());
        testParameter.setAbbreviation(request.getAbbreviation());
        testParameter.setDescription(request.getDescription());
        testParameter.setCreatedByUserId(SecurityUtils.getCurrentUserId());
        testParameter.setCreatedAt(LocalDateTime.now());

        TestParameter savedTestParameter = testParameterRepository.save(testParameter);
        return testParameterMapper.mapToResponse(savedTestParameter);
    }

    @Override
    public TestParameterResponse getTestParameterByAbbreviation(String abbreviation) {
        log.info("Getting test parameter by abbreviation: {}", abbreviation);

        TestParameter testParameter = testParameterRepository.findByAbbreviation(abbreviation.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Test parameter not found with abbreviation: " + abbreviation));

        return testParameterMapper.mapToResponse(testParameter);
    }

    @Override
    @Transactional
    public TestParameterResponse updateTestParameter(String testParameterId, UpdateTestParameterRequest request) {
        log.info("Updating test parameter with ID: {}", testParameterId);

        TestParameter existingTestParameter = testParameterRepository.findById(testParameterId)
                .orElseThrow(() -> new NotFoundException("Test parameter not found with ID: " + testParameterId));

        String newName = request.getParamName() != null ? request.getParamName().trim() : null;
        String newAbbreviation = request.getAbbreviation() != null
                ? request.getAbbreviation().trim().toUpperCase()
                : null;
        String newDescription = request.getDescription() != null ? request.getDescription().trim() : null;

        // Check unique
        if (newName != null
                && !newName.equals(existingTestParameter.getParamName())
                && testParameterRepository.existsByParamName(newName)) {
            throw new AlreadyExistsException("Test parameter with name '" + newName + "' already exists");
        }

        if (newAbbreviation != null
                && !newAbbreviation.equals(existingTestParameter.getAbbreviation())
                && testParameterRepository.existsByAbbreviation(newAbbreviation)) {
            throw new AlreadyExistsException("Test parameter with abbreviation '" + newAbbreviation + "' already exists");
        }

        if (newName != null) {
            existingTestParameter.setParamName(newName);
        }

        if (newAbbreviation != null) {
            existingTestParameter.setAbbreviation(newAbbreviation);
        }

        if (newDescription != null) {
            existingTestParameter.setDescription(newDescription);
        }

        existingTestParameter.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        existingTestParameter.setUpdatedAt(LocalDateTime.now());

        TestParameter updatedTestParameter = testParameterRepository.save(existingTestParameter);

        log.info("Test parameter updated successfully with ID: {}", updatedTestParameter.getId());

        return testParameterMapper.mapToResponse(updatedTestParameter);
    }

    @Override
    @Transactional
    public void deleteTestParameter(String testParameterId) {
        log.info("Deleting test parameter with ID: {}", testParameterId);

        // Tìm kiếm TestParameter hiện có
        TestParameter existingTestParameter = testParameterRepository.findById(testParameterId)
                .orElseThrow(() -> new NotFoundException("Test parameter not found with ID: " + testParameterId));

        // Xóa mềm tham số thử nghiệm và tất cả các phạm vi tham số liên quan
        existingTestParameter.setDeleted(true);
        existingTestParameter.setDeletedAt(LocalDateTime.now());
        existingTestParameter.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        existingTestParameter.setUpdatedAt(LocalDateTime.now());

        // Xóa mềm tất cả các phạm vi tham số liên quan
        if (existingTestParameter.getParameterRanges() != null && !existingTestParameter.getParameterRanges().isEmpty()) {
            existingTestParameter.getParameterRanges().forEach(parameterRange -> {
                parameterRange.setDeleted(true);
                parameterRange.setDeletedAt(LocalDateTime.now());
                parameterRange.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
                parameterRange.setUpdatedAt(LocalDateTime.now());
            });
        }

        testParameterRepository.save(existingTestParameter);

        log.info("Test parameter and associated parameter ranges deleted successfully with ID: {}", testParameterId);
    }

    @Override
    @Transactional
    public TestParameterResponse restoreTestParameter(String testParameterId) {
        log.info("Restoring test parameter with ID: {}", testParameterId);

        // Tìm tham số kiểm tra đã xóa (bao gồm cả tham số đã xóa mềm)
        TestParameter deletedTestParameter = testParameterRepository.findByIdIncludingDeleted(testParameterId)
                .orElseThrow(() -> new NotFoundException("Test parameter not found with ID: " + testParameterId));

        // Nếu tham số kiểm tra không bị xóa, ném ngoại lệ
        if (!deletedTestParameter.isDeleted()) {
            throw new IllegalArgumentException("Test parameter with ID: " + testParameterId + " is not deleted");
        }

        // Kiểm tra xung đột tên và viết tắt với các tham số kiểm tra hiện có
        if (testParameterRepository.existsByParamNameAndDeletedFalse(deletedTestParameter.getParamName())) {
            throw new AlreadyExistsException("Cannot restore: Test parameter with name '"
                    + deletedTestParameter.getParamName() + "' already exists");
        }

        if (testParameterRepository.existsByAbbreviationAndDeletedFalse(deletedTestParameter.getAbbreviation())) {
            throw new AlreadyExistsException("Cannot restore: Test parameter with abbreviation '"
                    + deletedTestParameter.getAbbreviation() + "' already exists");
        }

        // Khôi phục tham số kiểm tra
        deletedTestParameter.setDeleted(false);
        deletedTestParameter.setDeletedAt(null);
        deletedTestParameter.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
        deletedTestParameter.setUpdatedAt(LocalDateTime.now());

        // Khôi phục tất cả các phạm vi tham số liên quan
        if (deletedTestParameter.getParameterRanges() != null && !deletedTestParameter.getParameterRanges().isEmpty()) {
            deletedTestParameter.getParameterRanges().forEach(parameterRange -> {
                if (parameterRange.isDeleted()) {
                    parameterRange.setDeleted(false);
                    parameterRange.setDeletedAt(null);
                    parameterRange.setUpdatedByUserId(SecurityUtils.getCurrentUserId());
                    parameterRange.setUpdatedAt(LocalDateTime.now());
                }
            });
        }

        TestParameter restoredTestParameter = testParameterRepository.save(deletedTestParameter);

        log.info("Test parameter and associated parameter ranges restored successfully with ID: {}", restoredTestParameter.getId());

        return testParameterMapper.mapToResponse(restoredTestParameter);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TestParameterResponse> getAllTestParameters(int page, int size, String[] sort, String search, LocalDate startDate, LocalDate endDate) {
        // Validate and build Sort object
        Sort validSort = SortUtils.buildSort(
                sort,
                SortFields.TEST_PARAMETER_SORT_FIELDS,
                SortFields.DEFAULT_TEST_PARAMETER_SORT
        );

        // Create Pageable
        Pageable pageable = PageRequest.of(page, size, validSort);

        // Build specification
        Specification<TestParameter> spec = testParameterSpecification.build(search, startDate, endDate);

        // Query non-deleted test parameters with pagination
        Page<TestParameter> testParameterPage = testParameterRepository.findAll(spec, pageable);

        // Map to response DTOs
        Page<TestParameterResponse> dtoPage = testParameterPage.map(testParameterMapper::mapToResponse);

        // Create FilterInfo
        FilterInfo filterInfo = FilterInfo.builder()
                .search(search)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Create PageResponse
        PageResponse<TestParameterResponse> pageResponse = PageResponse.from(dtoPage, filterInfo);

        log.info("Retrieved {} test parameters out of {} total",
                testParameterPage.getNumberOfElements(),
                testParameterPage.getTotalElements());

        return pageResponse;
    }

    @Override
    public TestParameterResponse getTestParameterByTestParameterId(String testParameterId) {
        log.info("Getting test parameter by ID: {}", testParameterId);

        TestParameter testParameter = testParameterRepository.findById(testParameterId)
                .orElseThrow(() -> new NotFoundException("Test parameter not found with ID: " + testParameterId));

        return testParameterMapper.mapToResponse(testParameter);
    }

    @Override
    public boolean validateTestParametersExist(List<String> ids) {
        if (ids == null || ids.isEmpty()) return true;
        List<TestParameter> found = testParameterRepository.findAllByIdIn(ids);
        return found.size() == ids.size(); // Nếu tìm thấy đủ số lượng ID nghĩa là tất cả đều tồn tại
    }
}
