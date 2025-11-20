/*
 * @ {#} TestCatalogController.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.controllers;

import fit.test_order_service.dtos.response.ApiResponse;
import fit.test_order_service.dtos.response.TestCatalogResponse;
import fit.test_order_service.services.TestCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * @description: REST controller for TestCatalog operations
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@RestController
@RequestMapping("/api/v1/test-catalog")
@RequiredArgsConstructor
public class TestCatalogController {

    private final TestCatalogService testCatalogService;
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TestCatalogResponse>>> searchTests(
            @RequestParam String keyword) {

        List<TestCatalogResponse> tests = testCatalogService
                .findByTestNameContainingIgnoreCaseAndActiveTrue(keyword);

        return ResponseEntity.ok(
                ApiResponse.<List<TestCatalogResponse>>builder()
                        .success(true)
                        .data(tests)
                        .message("Search completed successfully")
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TestCatalogResponse>>> getAllActiveTests() {
        List<TestCatalogResponse> tests = testCatalogService.findByActiveTrueOrderByTestName();
        return ResponseEntity.ok(
                ApiResponse.<List<TestCatalogResponse>>builder()
                        .success(true)
                        .data(tests)
                        .message("Retrieved all active tests successfully")
                        .build()
        );
    }
}
