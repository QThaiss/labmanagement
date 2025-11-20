/*
 * @ {#} TestParameterRepository.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.TestParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository interface for TestParameter entity
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
@Repository
public interface TestParameterRepository extends JpaRepository<TestParameter, String>, JpaSpecificationExecutor<TestParameter> {
    /**
     * Kiểm tra sự tồn tại của TestParameter dựa trên paramName
     *
     * @param paramName tên tham số của TestParameter
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    boolean existsByParamName(String paramName);

    /**
     * Kiểm tra sự tồn tại của TestParameter dựa trên abbreviation
     *
     * @param abbreviation viết tắt của TestParameter
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    boolean existsByAbbreviation(String abbreviation);

    /**
     * Tìm TestParameter theo abbreviation
     *
     * @param abbreviation viết tắt của TestParameter
     *
     * @return Optional chứa TestParameter nếu tìm thấy, ngược lại là rỗng
     */
    Optional<TestParameter> findByAbbreviation(String abbreviation);

    /**
     * Tìm TestParameter theo id bao gồm cả những bản ghi đã bị xóa mềm
     *
     * @param id id của TestParameter
     *
     * @return Optional chứa TestParameter nếu tìm thấy, ngược lại là rỗng
     */
    @Query("SELECT tp FROM TestParameter tp WHERE tp.id = :id")
    Optional<TestParameter> findByIdIncludingDeleted(@Param("id") String id);

    /**
     * Kiểm tra sự tồn tại của TestParameter dựa trên paramName, loại trừ các bản ghi đã bị xóa mềm
     *
     * @param paramName tên tham số của TestParameter
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END " +
           "FROM TestParameter tp " +
           "WHERE tp.paramName = :paramName AND tp.isDeleted = false")
    boolean existsByParamNameAndDeletedFalse(String paramName);

    /**
     * Kiểm tra sự tồn tại của TestParameter dựa trên abbreviation, loại trừ các bản ghi đã bị xóa mềm
     *
     * @param abbreviation viết tắt của TestParameter
     *
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END " +
           "FROM TestParameter tp " +
           "WHERE tp.abbreviation = :abbreviation AND tp.isDeleted = false")
    boolean existsByAbbreviationAndDeletedFalse(String abbreviation);

    List<TestParameter> findAllByIdIn(List<String> ids);
}
