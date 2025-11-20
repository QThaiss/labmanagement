/*
 * @ (#) Configuration.java    1.0    12/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.entities;/*
 * @description:
 * @author: Bao Thong
 * @date: 12/11/2025
 * @version: 1.0
 */

import fit.instrument_service.enums.ConfigurationType;
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
@Document(collection = "configurations")
// Lưu trữ các cấu hình chung và cấu hình riêng cho từng loại thiết bị
public class Configuration extends BaseDocument {

    @Id
    private String id;

    @Field("name")
    private String name; // Tên cấu hình, vd: "Cấu hình chung v1.0"

    @Field("config_type")
    private ConfigurationType configType; // "General" hoặc "Specific" (Req 3.6.3.1)

    @Field("instrument_model")
    private String instrumentModel; // Áp dụng nếu là "Specific"

    @Field("instrument_type")
    private String instrumentType; // Áp dụng nếu là "Specific"

    @Field("version")
    private String version;

    @Field("settings")
    private Map<String, Object> settings; // Cấu trúc JSON linh hoạt cho các cài đặt (Req 3.6.3.1)
}
