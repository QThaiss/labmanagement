/*
 * @ {#} IamFeignClient.java   1.0     07/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.client;

import fit.patient_service.client.dtos.UserInternalResponse;
import fit.patient_service.configs.FeignClientConfig;
import fit.patient_service.dtos.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * @description: Feign client for interacting with the IAM service
 * @author: Tran Hien Vinh
 * @date:   07/10/2025
 * @version:    1.0
 */
@FeignClient(
        name = "iam-service",
        configuration = FeignClientConfig.class
)
public interface IamFeignClient {
    @GetMapping("/api/v1/internal/iam/users/{id}")
    ApiResponse<UserInternalResponse> getUserById(@PathVariable("id") String id);
}
