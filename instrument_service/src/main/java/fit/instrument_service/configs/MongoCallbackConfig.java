/*
 * @ {#} MongoCallbackConfig.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.configs;

import fit.instrument_service.entities.BaseDocument;
import fit.instrument_service.markers.HasBusinessId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;

/*
 * @description: Configuration class for MongoDB callbacks.
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Configuration
public class MongoCallbackConfig {
    @Bean
    public BeforeConvertCallback<BaseDocument> businessIdAutoGeneratorCallback() {
        return (entity, collection) -> {
            if (entity instanceof HasBusinessId businessEntity) {
                businessEntity.assignBusinessId();
            }
            return entity;
        };
    }
}
