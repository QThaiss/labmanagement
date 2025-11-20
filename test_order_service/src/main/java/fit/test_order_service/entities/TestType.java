package fit.test_order_service.entities;

import fit.test_order_service.utils.TestOrderGenerator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.time.ZoneOffset;

/** TestType entity */
@Entity
@Table(name = "test_type", indexes = {
        @Index(name = "idx_test_type_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Builder
public class TestType {

    @Id
    @Column(name = "type_id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 250)
    private String description;

    // The logic to fetch test parameters from the warehouse will be in the service layer (e.g., TestTypeService)
    // The field stores the list of required test parameters (e.g., as a JSON string or comma-separated IDs)
    @Column(name = "test_parameters_json", columnDefinition = "TEXT")
    private String testParametersJson;

    @Column(name = "reagent_name", length = 100)
    private String reagentName;

    @Column(name = "required_volume", nullable = false)
    private double requiredVolume;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "datetime(6)", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", columnDefinition = "datetime(6)")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;


    @PrePersist
    void prePersist() {
        if (id == null) {
            // Generate a UUID for the primary key
            id = TestOrderGenerator.generateTestTypeId();
        }
        // Setting createdAt time in UTC for consistency, although @CreationTimestamp should handle it
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }


}