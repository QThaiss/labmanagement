/*
 * @ (#) DeleteRawResultRequest.java    1.0    29/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.dtos.request;/*
 * @description:
 * @author: Bao Thong
 * @date: 29/11/2025
 * @version: 1.0
 */

import lombok.Data;
import java.util.List;

@Data
public class DeleteRawResultRequest {
    // Danh sách ID của các bản ghi RawTestResult cần xóa
    private List<String> rawResultIds;

    // Lý do xóa (nếu cần audit chi tiết)
    private String reason;
}
