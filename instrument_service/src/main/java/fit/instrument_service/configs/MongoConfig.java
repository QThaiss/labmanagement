/*
 * @ {#} MongoConfig.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/*
 * @description: Configures MongoDB auditing to automatically manage created and modified timestamps.
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorAware")
public class MongoConfig {
}
