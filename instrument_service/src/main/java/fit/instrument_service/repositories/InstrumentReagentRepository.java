
/*
 * @ {#} InstrumentReagentRepository.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.repositories;

import fit.instrument_service.entities.InstrumentReagent;
import fit.instrument_service.enums.ReagentStatus;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * @description: Repository quản lý các hóa chất của thiết bị trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Repository
public interface InstrumentReagentRepository extends MongoRepository<InstrumentReagent, String> {

    List<InstrumentReagent> findByInstrumentIdAndStatus(String instrumentId, ReagentStatus status);

    List<InstrumentReagent> findByInstrumentId(String instrumentId);

    // Tìm hóa chất bằng ID và ID thiết bị (để đảm bảo đúng)
    Optional<InstrumentReagent> findByIdAndInstrumentId(String id, String instrumentId);

    List<InstrumentReagent> findByInstrumentIdAndLotNumberAndIsDeletedFalse(String instrumentId, String lotNumber);
}
