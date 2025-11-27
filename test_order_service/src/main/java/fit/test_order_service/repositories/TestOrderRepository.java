/*
 * @ {#} TestRepository.java   1.0     11/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.repositories;

import fit.test_order_service.entities.TestOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for Test entity
 * @author: Tran Hien Vinh
 * @date:   11/10/2025
 * @version:    1.0
 */
@Repository
public interface TestOrderRepository extends JpaRepository<TestOrder, String> {
    /**
     * Tìm một TestOrder theo ID, chỉ trả về kết quả nếu nó chưa bị soft-delete (isDeleted = false).
     *
     * @param id ID của TestOrder cần tìm.
     * @return một Optional chứa TestOrder nếu tìm thấy và chưa bị xóa, ngược lại trả về Optional rỗng.
     */
    Optional<TestOrder> findByOrderIdAndDeletedFalse(String id);

    /**
     * Tìm một TestOrder theo mã đơn hàng (orderCode).
     *
     * @param orderCode Mã đơn hàng của TestOrder cần tìm.
     * @return một Optional chứa TestOrder nếu tìm thấy, ngược lại trả về Optional rỗng.
     */
    Optional<TestOrder> findByOrderCode(String orderCode);

    /**
     * Tìm tất cả TestOrder thỏa mãn Specification đã cho, với phân trang.
     *
     * @param spec     Specification để lọc TestOrder.
     * @param pageable Thông tin phân trang.
     * @return Một trang chứa các TestOrder thỏa mãn điều kiện.
     */
    Page<TestOrder> findAll(Specification<TestOrder> spec, Pageable pageable);


    @Query("""
                SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                FROM TestOrder t
                WHERE t.orderId = :orderId
            """)
    boolean existsByOrderId(String orderId);

    Optional<TestOrder> findByOrderId(String orderId);

    long countByOrderIdInAndDeletedFalse(List<String> orderIds);

    List<TestOrder> findByOrderIdInAndDeletedFalse(List<String> orderIds);

    List<TestOrder> findByDeletedFalseAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<TestOrder> findByDeletedFalse();

    Optional<TestOrder> findByBarcode(String barcode);

    // Thêm query tìm các đơn hàng quá hạn chưa có kết quả
    @Query("SELECT t FROM TestOrder t WHERE t.status IN ('PENDING', 'PROCESSING') AND t.createdAt < :timeout")
    List<TestOrder> findStuckOrders(@Param("timeout") LocalDateTime timeout);
}

