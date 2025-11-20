package fit.test_order_service.repositories;

import fit.test_order_service.entities.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTypeRepository extends JpaRepository<TestType, String> {
    boolean existsByName(String name);
}