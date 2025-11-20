/*
 * @ (#) ConfigurationSettingRepository.java    1.0    29/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.entities.ConfigurationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationSettingRepository extends JpaRepository<ConfigurationSetting, String>, JpaSpecificationExecutor<ConfigurationSetting> {
    /**
     * Kiểm tra xem một cấu hình với tên cho trước đã tồn tại hay chưa.
     *
     * @param name Tên của cấu hình.
     * @return true nếu tồn tại, false nếu không.
     */
    boolean existsByName(String name);

    /**
     * Tìm kiếm một ConfigurationSetting theo ID và đảm bảo nó chưa bị xóa.
     *
     * @param id ID của cấu hình.
     * @return Optional chứa ConfigurationSetting nếu tìm thấy và chưa bị xóa.
     */
    Optional<ConfigurationSetting> findByIdAndDeletedAtIsNull(String id);
}
