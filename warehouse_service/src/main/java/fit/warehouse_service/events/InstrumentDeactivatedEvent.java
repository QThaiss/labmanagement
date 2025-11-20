/*
 * @ {#} InstrumentDeactivatedEvent.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
 * @description: Event representing the deactivation of an Instrument.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentDeactivatedEvent implements Serializable {
    private String id;
}
