package fit.test_order_service.repositories;

import fit.test_order_service.entities.OrderCommentEditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCommentEditLogRepository extends JpaRepository<OrderCommentEditLog, String> {
}
