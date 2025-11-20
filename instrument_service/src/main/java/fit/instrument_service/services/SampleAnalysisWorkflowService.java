/*
 * @ {#} SampleAnalysisWorkflowService.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.services;

import fit.instrument_service.dtos.request.InitiateWorkflowRequest;
import fit.instrument_service.dtos.response.SampleResponse;
import fit.instrument_service.dtos.response.WorkflowResponse;

import java.util.List;

/*
 * @description: Service quản lý quy trình phân tích mẫu trong hệ thống
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
public interface SampleAnalysisWorkflowService {
   /**
     * Khởi tạo quy trình phân tích mẫu
     *
     * @param request Kiểu dữ liệu yêu cầu khởi tạo quy trình
     * @return Kiểu dữ liệu phản hồi quy trình
     */
    WorkflowResponse initiateWorkflow(InitiateWorkflowRequest request);

    /**
     * Xử lý cassette tiếp theo trong quy trình
     *
     * @param instrumentId ID của thiết bị
     * @return Kiểu dữ liệu phản hồi quy trình
     */
    WorkflowResponse processNextCassette(String instrumentId);

    /**
     * Lấy trạng thái của quy trình
     *
     * @param workflowId ID của quy trình
     * @return Kiểu dữ liệu phản hồi quy trình
     */
    WorkflowResponse getWorkflowStatus(String workflowId);

    /**
     * Lấy danh sách mẫu trong quy trình
     *
     * @param workflowId ID của quy trình
     * @return Danh sách mẫu phản hồi
     */
    List<SampleResponse> getWorkflowSamples(String workflowId);
}
