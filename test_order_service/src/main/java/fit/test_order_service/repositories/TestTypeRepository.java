package fit.test_order_service.repositories;

import fit.test_order_service.entities.TestType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestTypeRepository extends JpaRepository<TestType, String> {
    boolean existsByName(String name);

    @Query("SELECT t FROM TestType t WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TestType> searchTestTypes(@Param("search") String search, Pageable pageable);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);
}