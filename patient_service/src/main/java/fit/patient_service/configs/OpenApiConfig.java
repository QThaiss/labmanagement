/*
 * @ {#} OpenApiConfig.java   1.0     25/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   25/09/2025
 * @version:    1.0
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Patient Service API")
                        .version("1.0")
                        .description("API documentation for the Patient Service"))
                .servers(List.of(new Server()
                        .url("http://localhost:8080/patient-service")
                        .description("Local server")));
//                .components(new Components().addSecuritySchemes("bearerAuth",
//                        new SecurityScheme()
//                                .type(SecurityScheme.Type.HTTP)
//                                .scheme("bearer")
//                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi patientServiceApi() {
        return GroupedOpenApi.builder()
                .group("patient-service")
                .packagesToScan("fit.patient_service.controllers")
                .pathsToExclude("/accessLogs/**", "/medicalRecords/**", "/patients/**", "/profile/**")
                .build();
    }
}
