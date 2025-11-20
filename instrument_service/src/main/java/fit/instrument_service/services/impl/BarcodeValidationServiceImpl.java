/*
 * @ {#} BarcodeValidationServiceImpl.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services.impl;

import fit.instrument_service.services.BarcodeValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/*
 * @description: Service để xác thực mã vạch của mẫu máu
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@Service
@Slf4j
public class BarcodeValidationServiceImpl implements BarcodeValidationService {

    @Override
    public boolean isValidBarcode(String barcode) {
        // Xác thực cơ bản: không rỗng, chữ và số, độ dài từ 8-20 ký tự
        if (!StringUtils.hasText(barcode)) {
            log.debug("Barcode validation failed: empty or null");
            return false;
        }

        String trimmedBarcode = barcode.trim();

        // Check length
        if (trimmedBarcode.length() < 8 || trimmedBarcode.length() > 20) {
            log.debug("Barcode validation failed: invalid length {}", trimmedBarcode.length());
            return false;
        }

        // Kiểm tra xem chữ và số có dấu gạch ngang/gạch dưới có được phép không
        if (!trimmedBarcode.matches("^[A-Za-z0-9_-]+$")) {
            log.debug("Barcode validation failed: contains invalid characters");
            return false;
        }

        log.debug("Barcode validation passed for: {}", trimmedBarcode);
        return true;
    }
}
