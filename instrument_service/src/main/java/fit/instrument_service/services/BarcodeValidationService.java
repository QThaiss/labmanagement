/*
 * @ {#} BarcodeValidationService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

/*
 * @description: Service để xác thực mã vạch của mẫu máu
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface BarcodeValidationService {
    /*
     * Xác thực mã vạch của mẫu máu
     *
     * @param barcode Mã vạch cần xác thực
     * @return true nếu mã vạch hợp lệ, false nếu không hợp lệ
     */
    boolean isValidBarcode(String barcode);
}
