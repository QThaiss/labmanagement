/*
 * @ {#} RawTestResultRepository.java   1.0     13/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.RawTestResult;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/*
 * @description: Repository quản lý các kết quả xét nghiệm thô trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   13/11/2025
 * @version:    1.0
 */
@Repository
public interface RawTestResultRepository extends MongoRepository<RawTestResult, String> {
    List<RawTestResult> findByBarcodeIn(List<String> barcodes);

    /**
     * SRS 3.6.1.6: Tìm các kết quả thô đã sẵn sàng xóa (đã backup)
     * và cũ hơn thời gian quy định (createdDate < threshold).
     */
    @Query("{ 'is_ready_for_deletion' : true, 'created_date' : { $lt: ?0 } }")
    List<RawTestResult> findDeletableOldResults(LocalDateTime threshold);
}
