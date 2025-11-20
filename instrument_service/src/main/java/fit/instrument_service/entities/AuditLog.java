/*
 * @ (#) AuditLog.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditLogs")
public class AuditLog extends BaseDocument {

    @Id
    private String id;

    @Field("action")
    private AuditAction action; // Loại hành động (Req 3.6.1.5, 3.6.1.6, 3.6.2, 3.6.3.1)

    @Field("entity_type")
    private String entityType; // Tên entity, vd: "RawTestResult"

    @Field("entity_id")
    private String entityId; // 'id' hoặc 'barcode' của entity bị ảnh hưởng

    @Field("details")
    private Map<String, Object> details; // Thông tin chi tiết (vd: barcode, lý do)

    // Ghi chú: "user" và "timestamp" đã được xử lý bởi
    // 'createdBy' và 'createdAt' trong BaseDocument
}
