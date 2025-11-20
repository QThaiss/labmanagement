package fit.test_order_service.services;

import fit.test_order_service.dtos.request.CreateTestTypeRequest;
import fit.test_order_service.dtos.request.UpdateTestTypeRequest;
import fit.test_order_service.dtos.response.PageResponse;
import fit.test_order_service.dtos.response.TestTypeResponse;

import java.util.List;

public interface TestTypeService {
    TestTypeResponse createTestType(CreateTestTypeRequest request);
    TestTypeResponse getTestTypeById(String testTypeId);
    PageResponse<TestTypeResponse> getAllTestTypes(int page, int size, String[] sort, String search);
    TestTypeResponse updateTestType(String testTypeId, UpdateTestTypeRequest request);


}