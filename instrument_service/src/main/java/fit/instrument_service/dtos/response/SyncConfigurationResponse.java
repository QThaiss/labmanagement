/*
 * @ {#} SyncConfigurationResponse.java   1.0     25/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/*
 * @description: Response DTO for synchronizing instrument configurations.
 * @author: Tran Hien Vinh
 * @date:   25/11/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConfigurationResponse {
    private String instrumentId;
    private String instrumentModel;
    private String instrumentType;
    private String generalConfigId;
    private String specificConfigId;
    private Map<String, Object> generalSettings;
    private Map<String, Object> specificSettings;
    private Map<String, Object> appliedSettings;
    private boolean fullySynced;
    private List<String> warnings;
}
