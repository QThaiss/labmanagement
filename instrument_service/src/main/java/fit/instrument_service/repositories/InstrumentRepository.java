/*
 * @ {#} InstrumentRepository.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.Instrument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/*
 * @description: Repository interface for managing Instrument entities in MongoDB.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Repository
public interface InstrumentRepository extends MongoRepository<Instrument, String> {
}
