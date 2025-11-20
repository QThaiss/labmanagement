package fit.test_order_service.client;

import fit.test_order_service.client.dtos.ReagentDeductionRequest;
import fit.test_order_service.client.dtos.ReagentDeductionResponse;
import fit.test_order_service.dtos.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "warehouse-service", url = "${application.config.warehouse-service-url}")
public interface WarehouseFeignClient {

    @PostMapping("/api/v1/warehouse/reagents/deduct")
    ApiResponse<ReagentDeductionResponse> checkAndDeductReagent(@RequestBody ReagentDeductionRequest request);

    @PostMapping("/api/v1/warehouse/test-parameters/validate-ids")
    ApiResponse<Boolean> validateTestParameters(@RequestBody List<String> ids);
}