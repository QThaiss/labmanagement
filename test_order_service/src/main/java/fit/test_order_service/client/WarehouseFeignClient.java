package fit.test_order_service.client;

import fit.test_order_service.client.dtos.ReagentDeductionRequest;
import fit.test_order_service.client.dtos.ReagentDeductionResponse;
import fit.test_order_service.client.dtos.TestParameterResponse;
import fit.test_order_service.dtos.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "warehouse-service", url = "${application.config.warehouse-service-url}")
public interface WarehouseFeignClient {

    @PostMapping("/api/v1/warehouse/reagents/deduct")
    ApiResponse<ReagentDeductionResponse> checkAndDeductReagent(@RequestBody ReagentDeductionRequest request);

    @GetMapping("/api/v1/warehouse/reagents/check-availability")
    ApiResponse<Boolean>checkReagentAvailability(@RequestParam String reagentName, @RequestParam  Double requiredVolume);

    @PostMapping("/api/v1/warehouse/test-parameters/validate-ids")
    ApiResponse<Boolean> validateTestParameters(@RequestBody List<String> ids);

    @GetMapping("/api/v1/warehouse/test-parameters/{testParameterId}")
    ApiResponse<TestParameterResponse> getTestParameterByTestParameterId(@PathVariable String testParameterId);
}