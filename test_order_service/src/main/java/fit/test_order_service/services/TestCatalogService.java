/*
 * @ {#} TestCatalogService.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services;

import fit.test_order_service.dtos.response.TestCatalogResponse;
import fit.test_order_service.entities.TestCatalog;

import java.util.List;

/*
 * @description: Service interface for TestCatalog operations
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
public interface TestCatalogService {
    /**
     * Tìm kiếm danh mục xét nghiệm theo tên chứa từ khóa, chỉ bao gồm các xét nghiệm đang hoạt động.
     *
     * @param keyword từ khóa để tìm kiếm trong tên xét nghiệm
     * @return danh sách các phản hồi danh mục xét nghiệm phù hợp
     */
    List<TestCatalogResponse> findByTestNameContainingIgnoreCaseAndActiveTrue(String keyword);

    /**
     * Lấy tất cả các danh mục xét nghiệm đang hoạt động, sắp xếp theo tên xét nghiệm.
     *
     * @return danh sách các phản hồi danh mục xét nghiệm đang hoạt động
     */
    List<TestCatalogResponse> findByActiveTrueOrderByTestName();
}
