/*
 * @ {#} TestCatalogServiceImpl.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import fit.test_order_service.dtos.response.TestCatalogResponse;
import fit.test_order_service.entities.TestCatalog;
import fit.test_order_service.mappers.TestCatalogMapper;
import fit.test_order_service.repositories.TestCatalogRepository;
import fit.test_order_service.services.TestCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @description: Implementation of TestCatalogService to handle TestCatalog operations.
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
public class TestCatalogServiceImpl implements TestCatalogService {
    private final TestCatalogRepository testCatalogRepository;

    private final TestCatalogMapper testCatalogMapper;

    @Override
    public List<TestCatalogResponse> findByTestNameContainingIgnoreCaseAndActiveTrue(String keyword) {
        List<TestCatalog> tests = testCatalogRepository
                .findByTestNameContainingIgnoreCaseAndActiveTrue(keyword);

        return tests.stream().map(testCatalogMapper::toResponse).toList();
    }

    @Override
    public List<TestCatalogResponse> findByActiveTrueOrderByTestName() {
        List<TestCatalog> tests = testCatalogRepository.findByActiveTrueOrderByTestName();

        return tests.stream().map(testCatalogMapper::toResponse).toList();
    }
}
