/*
 * @ {#} TestCatalogRepository.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.repositories;

import fit.test_order_service.entities.TestCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for TestCatalog entity
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Repository
public interface TestCatalogRepository extends JpaRepository<TestCatalog, Long> {
    /**
     * Tìm kiếm TestCatalog theo mã local (không phân biệt chữ hoa thường) và trạng thái active
     *
     * @param testCode mã local của test cần tìm
     * @return Optional chứa TestCatalog nếu tìm thấy
     */
    Optional<TestCatalog> findByLocalCodeIgnoreCaseAndActiveTrue(String testCode);

    /**
     * Tìm kiếm TestCatalog theo mã LOINC và trạng thái active
     *
     * @param loincCode mã LOINC của test cần tìm
     * @return Optional chứa TestCatalog nếu tìm thấy
     */
    Optional<TestCatalog> findByLoincCodeAndActiveTrue(String loincCode);

    /**
     * Tìm kiếm TestCatalog theo tên test (không phân biệt chữ hoa thường) và trạng thái active
     *
     * @param testName tên của test cần tìm
     * @return Optional chứa TestCatalog nếu tìm thấy
     */
    Optional<TestCatalog> findByTestNameIgnoreCaseAndActiveTrue(String testName);

    /**
     * Tìm kiếm tất cả TestCatalog có tên chứa chuỗi cho trước (không phân biệt chữ hoa thường) và trạng thái active
     *
     * @param testName chuỗi tên của test cần tìm
     * @return Danh sách TestCatalog thỏa mãn điều kiện
     */
    List<TestCatalog> findByTestNameContainingIgnoreCaseAndActiveTrue(String testName);

    /**
     * Lấy tất cả TestCatalog có trạng thái active, sắp xếp theo tên test
     *
     * @return Danh sách TestCatalog đã được sắp xếp
     */
    List<TestCatalog> findByActiveTrueOrderByTestName();
}
