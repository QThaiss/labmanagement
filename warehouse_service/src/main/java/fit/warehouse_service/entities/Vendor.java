package fit.warehouse_service.entities;

import fit.warehouse_service.utils.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vendors")
public class Vendor extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String contactPerson;

    @Column(unique = true)
    private String email;

    private String phone;

    @Column(length = 512)
    private String address;

    @Override
    public String generateId() {
        // Sử dụng IdGenerator của bạn để tạo ID với tiền tố "VDR"
        return IdGenerator.generate("VDR");
    }
}