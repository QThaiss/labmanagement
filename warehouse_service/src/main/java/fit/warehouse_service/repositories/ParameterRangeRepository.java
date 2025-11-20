/*
 * @ {#} ParameterRangeRepository.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.ParameterRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * @description: Repository interface for ParameterRange entity
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Repository
public interface ParameterRangeRepository extends JpaRepository<ParameterRange, String> {
    /**
     * Kiểm tra sự tồn tại của ParameterRange dựa trên testParameter
     *
     * @param testParameterId testParameterId của ParameterRange
     * @param gender gender của ParameterRange
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    boolean existsByTestParameterIdAndGender(String testParameterId, String gender);

    /**
     * Tìm ParameterRange theo id bao gồm cả những bản ghi đã bị xóa mềm
     *
     * @param id id của ParameterRange
     *
     * @return Optional chứa ParameterRange nếu tìm thấy, ngược lại là rỗng
     */
    @Query("SELECT pr FROM ParameterRange pr WHERE pr.id = :id")
    Optional<ParameterRange> findByIdIncludingDeleted(@Param("id") String id);

    /**
     * Kiểm tra sự tồn tại của ParameterRange dựa trên testParameter và gender, loại trừ các bản ghi đã bị xóa mềm
     *
     * @param testParameterId testParameterId của ParameterRange
     * @param gender gender của ParameterRange
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END " +
           "FROM ParameterRange pr " +
           "WHERE pr.testParameter.id = :testParameterId " +
           "AND pr.gender = :gender " +
           "AND pr.isDeleted = false")
    boolean existsByTestParameterIdAndGenderAndDeletedFalse(String testParameterId, String gender);

}
