/*
 * @ {#} TestCatalog.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.entities;

import jakarta.persistence.*;
import lombok.*;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
@Entity
@Table(name = "test_catalog")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loinc_code", length = 20, nullable = false)
    private String loincCode; // Mã LOINC (chuẩn quốc tế, ví dụ 2345-7)

    @Column(name = "local_code", length = 64)
    private String localCode; // Mã nội bộ (ví dụ GLU, HGB, WBC)

    @Column(name = "test_name", length = 128, nullable = false)
    private String testName; // Tên xét nghiệm

    @Column(name = "specimen_type", length = 64)
    private String specimenType; // Loại mẫu (Blood, Serum, Urine, ...)

    @Column(name = "unit", length = 32)
    private String unit; // Đơn vị đo (mg/dL, g/dL, 10^3/uL...)

    @Column(name = "reference_range", length = 64)
    private String referenceRange; // Khoảng tham chiếu bình thường

    @Column(name = "method", length = 128)
    private String method; // Phương pháp (Enzymatic, Colorimetric, ...)

    @Column(name = "active")
    private Boolean active; // Cho phép sử dụng
}
