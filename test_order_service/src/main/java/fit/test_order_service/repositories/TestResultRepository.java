package fit.test_order_service.repositories;

import fit.test_order_service.entities.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, String> {


    @Query("""
                    SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END
                    FROM TestResult tr
                    WHERE tr.resultId = :testResultId       
            """)
    boolean existsByResultId(String testResultId);

    /**
     * Tìm kiếm TestResult dựa trên orderId và analyteName (không phân biệt hoa thường).
     * Được sử dụng trong logic 'reviewTestOrder' để tìm kết quả cần điều chỉnh.
     *
     * @param orderId     ID của TestOrder
     * @param analyteName Tên của Analyte (ví dụ: "HDL Cholesterol")
     * @return Danh sách các TestResult khớp (thường chỉ là 1)
     */
    List<TestResult> findByOrderIdAndAnalyteNameIgnoreCase(String orderId, String analyteName);
}
