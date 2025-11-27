/*
 * @ {#} EventPublisherService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.events.*;

/*
 * @description: Service interface for publishing instrument-related events.
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface EventPublisherService {
    /**
     * Xuất bản sự kiện khi một Instrument được kích hoạt.
     *
     * @param event The event containing details of the activated instrument.
     */
    void publishInstrumentActivated(InstrumentActivatedEvent event);

    /**
     * Xuất bản sự kiện khi một Instrument bị vô hiệu hóa.
     *
     * @param event The event containing details of the deactivated instrument.
     */
    void publishInstrumentDeactivated(InstrumentDeactivatedEvent event);

    /**
     * Xuất bản sự kiện khi một Configuration được tạo.
     *
     * @param event The event containing details of the created configuration.
     */
    void publishConfigurationCreated(ConfigurationCreatedEvent event);

    /**
     * Xuất bản sự kiện khi một Configuration bị xóa.
     *
     * @param event The event containing details of the deleted configuration.
     */
    void publishConfigurationDeleted(ConfigurationDeletedEvent event);

    void publishConfigurationUpdated(ConfigurationUpdatedEvent event);
}
