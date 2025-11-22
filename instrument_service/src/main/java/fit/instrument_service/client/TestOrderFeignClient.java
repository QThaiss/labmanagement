/*
 * @ {#} TestOrderFeignClient.java   1.0     12/11/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.instrument_service.client;

import fit.instrument_service.client.dtos.AutoCreateTestOrderRequest;
import fit.instrument_service.client.dtos.Hl7ProcessResponse;
import fit.instrument_service.client.dtos.TestOrderDetailResponse;
import fit.instrument_service.client.dtos.TestOrderResponse;
import fit.instrument_service.configs.FeignClientConfig;
import fit.instrument_service.dtos.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/*
 * @description: Feign client to interact with Test Order Service
 * @author: Tran Hien Vinh
 * @date:   12/11/2025
 * @version:    1.0
 */
@FeignClient(
        name = "test-order-service",
        configuration = FeignClientConfig.class
)
public interface TestOrderFeignClient {

    @GetMapping("/api/v1/internal/test-orders/{id}")
    ApiResponse<TestOrderResponse> getTestOrderById(@PathVariable("id") String id);

    @PostMapping("/api/v1/internal/test-orders")
    ApiResponse<TestOrderResponse> createTestOrder(@RequestBody Map<String, Object> testOrderData);

    @GetMapping("/api/v1/internal/test-orders/full/{id}")
    ApiResponse<TestOrderDetailResponse> getTestOrderDetailsById(@PathVariable("id") String id);

    @GetMapping("/api/v1/test-orders/{testOrderById}/full")
    ApiResponse<TestOrderResponse> getTestOrderByTestOrderId(@PathVariable String testOrderById);

    @PostMapping("/api/v1/test-orders/internal/auto-create")
    ApiResponse<TestOrderResponse> autoCreateTestOrder(@Validated @RequestBody AutoCreateTestOrderRequest request);

    @PostMapping(value = "/api/v1/hl7/process", consumes = MediaType.TEXT_PLAIN_VALUE)
    ApiResponse<Hl7ProcessResponse> publishHl7Result(@RequestBody String rawHl7Message);
}
