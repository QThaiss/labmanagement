/*
 * @ (#) Instrument.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.embedded.Vendor;
import fit.instrument_service.enums.InstrumentMode;
import fit.instrument_service.enums.InstrumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "instruments")
public class Instrument extends BaseDocument {

    @Id
    private String id;

    @Field("name")
    private String name; // Tên tùy chỉnh

    @Field("model")
    private String model; // Model thiết bị

    @Field("type")
    private String type; // Loại thiết bị

    @Field("serial_number")
    @Indexed(unique = true)
    private String serialNumber; // Số seri_

    @Field("mode")
    private InstrumentMode mode;

    @Field("status")
    private InstrumentStatus status;

    @Field("last_mode_change_reason")
    private String lastModeChangeReason; // Lý do cho lần đổi mode cuối (Req 3.6.1.1)

    @Field("vendor")
    private Vendor vendor;
}
