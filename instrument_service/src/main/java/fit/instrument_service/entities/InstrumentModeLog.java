/*
 * @ (#) InstrumentModeLog.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.enums.InstrumentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "instrumentModeLogs")
public class InstrumentModeLog extends BaseDocument {

    @Id
    private String id;

    @Field("instrument_id")
    private String instrumentId; // Tham chiếu đến 'id' của Instrument

    @Field("previous_mode")
    private InstrumentMode previousMode; // Chế độ cũ (Req 3.6.1.1)

    @Field("new_mode")
    private InstrumentMode newMode; // Chế độ mới (Req 3.6.1.1)

    @Field("reason")
    private String reason; // Lý do thay đổi (Req 3.6.1.1)

    // Ghi chú: "user" và "timestamp" đã được xử lý bởi các trường
    // 'createdBy' và 'createdAt' trong BaseDocument
}
