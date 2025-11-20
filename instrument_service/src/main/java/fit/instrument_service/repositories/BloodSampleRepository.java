/*
 * @ {#} BloodSampleRepository.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.BloodSample;
import fit.instrument_service.enums.SampleStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository quản lý các mẫu máu trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Repository
public interface BloodSampleRepository extends MongoRepository<BloodSample, String> {

    Optional<BloodSample> findByBarcode(String barcode);

    List<BloodSample> findByWorkflowId(String workflowId);

    List<BloodSample> findByInstrumentIdAndStatus(String instrumentId, SampleStatus status);

    List<BloodSample> findByStatus(SampleStatus status);
}
