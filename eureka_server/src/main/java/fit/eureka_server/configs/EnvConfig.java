/*
 * @ {#} EnvConfig.java   1.0     30/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.eureka_server.configs;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/*
 * @description: Config environment variables from .env file
 * @author: Tran Hien Vinh
 * @date:   30/09/2025
 * @version:    1.0
 */
public class EnvConfig implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        Map<String, Object> vars = new HashMap<>();
        dotenv.entries().forEach(e -> vars.put(e.getKey(), e.getValue()));
        environment.getPropertySources().addFirst(new MapPropertySource("dotenv", vars));
    }
}
