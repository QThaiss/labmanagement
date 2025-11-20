/*
 * @ {#} AuditorAwareImpl.java   1.0     10/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.utils.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   10/11/2025
 * @version:    1.0
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            // Lấy userId từ JWT (qua SecurityUtils)
            String userId = SecurityUtils.getCurrentUserId();
            return Optional.ofNullable(userId);
        } catch (Exception e) {
            // Nếu không có token (vd: background task, system event)
            return Optional.of("SYSTEM");
        }
    }
}
