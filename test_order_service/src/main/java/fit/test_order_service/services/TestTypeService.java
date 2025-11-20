package fit.test_order_service.services;

import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.response.TestTypeResponse;

public interface TestTypeService {
    TestTypeResponse createTestType(CreateTestTypeRequest request);
}