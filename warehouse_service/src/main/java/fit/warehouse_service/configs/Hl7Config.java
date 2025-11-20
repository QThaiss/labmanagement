/*
 * @ {#} Hl7Config.java   1.0     21/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.configs;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * @description: HL7 Configuration for HAPI context and parser
 * @author: Tran Hien Vinh
 * @date:   21/10/2025
 * @version:    1.0
 */
@Configuration
public class Hl7Config {

    @Bean
    public HapiContext hapiContext() {
        HapiContext context = new DefaultHapiContext();
        // Disable validation for better performance
        context.setValidationContext(new NoValidation());
        return context;
    }

    @Bean
    public Parser hl7Parser(HapiContext hapiContext) {
        return hapiContext.getPipeParser();
    }
}
