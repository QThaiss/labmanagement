package fit.instrument_service.client;

import fit.instrument_service.client.dtos.ReagentLotStatusResponse;
import fit.instrument_service.dtos.response.ApiResponse;
import fit.instrument_service.dtos.response.VendorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "warehouse-service") // Tên của service đã đăng ký trên Eureka
public interface WarehouseFeignClient {


    @GetMapping("/api/v1/warehouse/reagents/history/validate-stock")
    ResponseEntity<Void> validateReagentStock(
            @RequestParam("vendorId") String vendorId,
            @RequestParam("lotNumber") String lotNumber);

    @GetMapping("/api/v1/warehouse/reagents/history/lot-status")
    ApiResponse<ReagentLotStatusResponse> getReagentLotStatus(@RequestParam("lotNumber") String lotNumber);

    @GetMapping("/api/v1/warehouse/vendors/{id}")
    ApiResponse<VendorResponse> getVendorById(@PathVariable("id") String id);
}