/*
 * @ {#} FlaggingConfigRule.java   1.0     22/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.entities;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/10/2025
 * @version:    1.0
 */
/*
 * @ (#) FlaggingConfigRule.java    1.0    22/10/2025
 * Copyright (c) 2025 IUH
 */

import fit.test_order_service.enums.FlagSeverity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "flagging_config_rule", indexes = {
        @Index(name = "idx_rule_flagcode", columnList = "flag_code, severity"),
        @Index(name = "idx_rule_configver", columnList = "config_version_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlaggingConfigRule {
    @Id
    @Column(name = "rule_id", length = 36, nullable = false, updatable = false)
    private String ruleId;

    @NotBlank
    @Column(name = "config_version_id", length = 36, nullable = false)
    private String configVersionId;

    @NotBlank
    @Size(max = 64)
    @Column(name = "flag_code", length = 64, nullable = false)
    private String flagCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 16, nullable = false)
    private FlagSeverity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_json", columnDefinition = "json")
    private String conditionJson;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    /* Relation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_version_id", referencedColumnName = "id", insertable = false, updatable = false)
    private FlaggingConfigVersion configVersionRef;

    @PrePersist
    void pp() {
        if (ruleId == null) ruleId = UUID.randomUUID().toString();
    }
}

