package fit.warehouse_service.repositories;

import fit.warehouse_service.entities.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, String>, JpaSpecificationExecutor<Vendor> {
    Optional<Vendor> findByName(String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);


}