/*
 * @ (#) SecurityBeans.java    1.0    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.configs;/*
 * @description:
 * @author: Bao Thong
 * @date: 01/10/2025
 * @version: 1.0
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeans {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
