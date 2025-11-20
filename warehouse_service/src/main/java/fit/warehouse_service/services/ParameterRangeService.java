/*
 * @ {#} ParameterRangeService.java   1.0     17/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.services;

import fit.warehouse_service.dtos.request.CreateParameterRangeRequest;
import fit.warehouse_service.dtos.request.UpdateParameterRangeRequest;
import fit.warehouse_service.dtos.response.ParameterRangeResponse;

/*
 * @description: Service interface for managing ParameterRange entities
 * @author: Tran Hien Vinh
 * @date:   17/11/2025
 * @version:    1.0
 */
public interface ParameterRangeService {
    /**
     * Tạo mới một ParameterRange
     *
     * @param request dữ liệu yêu cầu tạo ParameterRange
     *
     * @return ParameterRangeResponse chứa thông tin của ParameterRange vừa tạo
     */
    ParameterRangeResponse createParameterRange(CreateParameterRangeRequest request);

    /**
     * Cập nhật một ParameterRange hiện có
     *
     * @param parameterRangeId id của ParameterRange cần cập nhật
     * @param request dữ liệu yêu cầu cập nhật ParameterRange
     *
     * @return ParameterRangeResponse chứa thông tin của ParameterRange vừa cập nhật
     */
    ParameterRangeResponse updateParameterRange(String parameterRangeId, UpdateParameterRangeRequest request);

    /**
     * Xóa mềm một ParameterRange
     *
     * @param parameterRangeId id của ParameterRange cần xóa
     */
    void deleteParameterRange(String parameterRangeId);

    /**
     * Khôi phục một ParameterRange đã bị xóa mềm
     *
     * @param parameterRangeId id của ParameterRange cần khôi phục
     *
     * @return ParameterRangeResponse chứa thông tin của ParameterRange vừa khôi phục
     */
    ParameterRangeResponse restoreParameterRange(String parameterRangeId);

    /**
     * Lấy thông tin một ParameterRange theo id
     *
     * @param parameterRangeId id của ParameterRange cần lấy
     *
     * @return ParameterRangeResponse chứa thông tin của ParameterRange
     */
    ParameterRangeResponse getParameterRangeById(String parameterRangeId);
}
