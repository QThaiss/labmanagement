/*
 * @ {#} EventSubscriberService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

import fit.instrument_service.events.ConfigurationCreatedEvent;
import fit.instrument_service.events.ConfigurationDeletedEvent;
import fit.instrument_service.events.InstrumentActivatedEvent;
import fit.instrument_service.events.InstrumentDeactivatedEvent;

/*
 * @description: Service interface for subscribing to instrument-related events.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface EventSubscriberService {
    /*
     * Xử lý sự kiện khi một thiết bị được kích hoạt
     *
     * @param event Sự kiện kích hoạt thiết bị
     */
    void handleInstrumentActivated(InstrumentActivatedEvent event);

    /*
     * Xử lý sự kiện khi một thiết bị bị vô hiệu hóa
     *
     * @param event Sự kiện vô hiệu hóa thiết bị
     */
    void handleInstrumentDeactivated(InstrumentDeactivatedEvent event);

    /**
     * Xử lý sự kiện khi một cấu hình được tạo.
     *
     * @param event Sự kiện tạo cấu hình
     */
    void handleConfigurationCreated(ConfigurationCreatedEvent event);

    /**
     * Xử lý sự kiện khi một cấu hình bị xóa.
     *
     * @param event Sự kiện xóa cấu hình
     */
    void handleConfigurationDeleted(ConfigurationDeletedEvent event);
}
