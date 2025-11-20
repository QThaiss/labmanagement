/*
 * @ {#} CassetteRepository.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.Cassette;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository quản lý các cassette trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Repository
public interface CassetteRepository extends MongoRepository<Cassette, String> {

    Optional<Cassette> findByCassetteIdentifier(String cassetteIdentifier);

    List<Cassette> findByInstrumentIdAndProcessedOrderByQueuePositionAsc(String instrumentId, boolean processed);

    List<Cassette> findByInstrumentIdOrderByQueuePositionAsc(String instrumentId);
}
