/*
 * @ (#) ConfigurationRepository.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.repositories;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.entities.Configuration;
import fit.instrument_service.enums.ConfigurationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends MongoRepository<Configuration, String> {
    /**
     * Tìm cấu hình mới nhất theo loại cấu hình.
     *
     * @param configType Loại cấu hình (GENERAL hoặc SPECIFIC).
     * @return Cấu hình mới nhất nếu tồn tại, ngược lại trả về Optional rỗng.
     */
    Optional<Configuration> findTopByConfigTypeOrderByVersionDesc(ConfigurationType configType);

    /**
     * Tìm cấu hình mới nhất theo loại cấu hình và mẫu thiết bị.
     *
     * @param configType      Loại cấu hình (GENERAL hoặc SPECIFIC).
     * @param instrumentModel Mẫu thiết bị.
     * @return Cấu hình mới nhất nếu tồn tại, ngược lại trả về Optional rỗng.
     */
    Optional<Configuration> findTopByConfigTypeAndInstrumentModelAndInstrumentTypeOrderByVersionDesc(ConfigurationType configType, String instrumentModel, String instrumentType);

    /**
     * Tìm cấu hình mới nhất theo loại cấu hình và loại thiết bị.
     *
     * @param configType     Loại cấu hình (GENERAL hoặc SPECIFIC).
     * @param instrumentType Loại thiết bị.
     * @return Cấu hình mới nhất nếu tồn tại, ngược lại trả về Optional rỗng.
     */
    Optional<Configuration> findTopByConfigTypeAndInstrumentTypeOrderByVersionDesc(ConfigurationType configType, String instrumentType);
}
