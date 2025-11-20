package fit.iam_service.repositories;

import fit.iam_service.entities.Privilege;
import fit.iam_service.enums.PrivilegeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivilegeRepository extends JpaRepository<Privilege, String> {
    Optional<Privilege> findByPrivilegeCodeAndIsDeletedFalse(PrivilegeCode code);
    Optional<Privilege> findByPrivilegeCode(String code);
    List<Privilege> findByPrivilegeCodeInAndIsDeletedFalse(List<String> codes);
}
