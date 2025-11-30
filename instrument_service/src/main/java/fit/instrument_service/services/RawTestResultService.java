/*
 * @ (#) RawTestResultService.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.services;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import fit.instrument_service.dtos.request.DeleteRawResultRequest;

public interface RawTestResultService {
    void deleteRawResults(DeleteRawResultRequest request);

    void executeAutoDeletion();
}
