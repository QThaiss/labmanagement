/*
 * @ (#) UserRepository.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import fit.iam_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByIdentifyNumber(String identifyNumber);

    boolean existsByUsername(String username);

    @Query(value = """ 
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END 
            FROM User u 
            WHERE u.username = :username AND u.isDeleted = false
            """)
    boolean existsByUsernameAndIsDeletedFalse(@Param("username") String username);

    boolean existsByEmailAndUserIdIsNot(String email, String id);

    boolean existsByPhoneAndUserIdIsNot(String phone, String id);

    boolean existsByIdentifyNumberAndUserIdIsNot(String identifyNumber, String id);

    // Lấy bản ghi đang hoạt động (chưa xóa) — tránh @Where mặc định bằng cách điều kiện rõ ràng
    @Query("select u from User u where u.userId = :id and u.isDeleted = false")
    Optional<User> findActiveById(@Param("id") String id);

    // Cập nhật xóa mềm + gán deletedBy
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.isDeleted = true, u.deletedAt = :now, u.deletedBy = :actorId " +
            "where u.userId = :id and u.isDeleted = false")
    int softDelete(@Param("id") String id,
                   @Param("actorId") String actorId,
                   @Param("now") LocalDateTime now);

    // Kiểm tra trạng thái xóa (bỏ qua @Where) — trả về null nếu không tồn tại
    @Query(value = "select is_deleted from users where user_id = :id", nativeQuery = true)
    Boolean isDeletedFlag(@Param("id") String id);

    // Tìm user kèm role, privilege cho mục đích xác thực
    @Query("""
            select distinct u from User u
            join fetch u.role r
            left join fetch r.rolePrivileges rp
            left join fetch rp.privilege p
            where u.username = :username and u.isDeleted = false
            """)
    Optional<User> findAuthUser(@Param("username") String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
            select u from User u
            left join fetch u.role r
            where u.userId = :id and u.isDeleted = false
            """)
    Optional<User> findActiveByIdFetchRole(@Param("id") String id);

    boolean existsByRole_RoleId(String roleId);
}