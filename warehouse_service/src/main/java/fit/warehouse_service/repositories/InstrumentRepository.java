/*
<<<<<<< HEAD
 * @ {#} InstrumentRepository.java   1.0     29/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * @description: Repository interface for managing Instrument entities.
 * @author: Tran Hien Vinh
 * @date:   29/10/2025
 * @version:    1.0
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, String>, JpaSpecificationExecutor<Instrument> {
    /**
     * Tìm kiếm một Instrument theo tên.
     *
     * @param name Tên của Instrument cần tìm.
     * @return Một Optional chứa Instrument nếu tìm thấy, ngược lại là rỗng.
     */
    Optional<Instrument> findByName(String name);

    /**
     * Kiểm tra sự tồn tại của một Instrument theo số serial.
     *
     * @param serialNumber Số serial của Instrument cần kiểm tra.
     * @return true nếu tồn tại, ngược lại là false.
     */
    boolean existsBySerialNumber(String serialNumber);

    boolean existsByIpAddressAndPort(String ipAddress, int port);
}
