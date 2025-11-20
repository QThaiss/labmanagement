/*
 * @ (#) Instrument.java    1.0    27/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.warehouse_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 27/10/2025
 * @version: 1.0
 */

import fit.warehouse_service.enums.InstrumentStatus;
import fit.warehouse_service.utils.IdGenerator;
import fit.warehouse_service.enums.ProtocolType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "instruments",
        uniqueConstraints = {
                // Đảm bảo cặp ipAddress và port là duy nhất trong DB
                @UniqueConstraint(columnNames = {"ipAddress", "port"})
        }
)
public class Instrument extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentStatus status;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private int port;

    @Enumerated(EnumType.STRING)
    private ProtocolType protocolType;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String type;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @ManyToMany
    @JoinTable(
            name = "instrument_compatible_reagents",
            joinColumns = @JoinColumn(name = "instrument_id"),
            inverseJoinColumns = @JoinColumn(name = "reagent_type_id")
    )
    private Set<ReagentType> compatibleReagents;

    @ManyToMany
    @JoinTable(
            name = "instrument_configurations",
            joinColumns = @JoinColumn(name = "instrument_id"),
            inverseJoinColumns = @JoinColumn(name = "setting_id")
    )
    private Set<ConfigurationSetting> configurations;

    @Override
    public String generateId() {
        return IdGenerator.generate("INS"); // Tiền tố "INS"
    }
}
