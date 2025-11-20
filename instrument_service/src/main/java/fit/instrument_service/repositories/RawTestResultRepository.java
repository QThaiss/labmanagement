/*
 * @ {#} RawTestResultRepository.java   1.0     13/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.RawTestResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/*
 * @description: Repository quản lý các kết quả xét nghiệm thô trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   13/11/2025
 * @version:    1.0
 */
@Repository
public interface RawTestResultRepository extends MongoRepository<RawTestResult, String> {
}
