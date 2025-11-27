package fit.warehouse_service.services;

import fit.warehouse_service.dtos.response.ReagentTypeResponse;
import java.util.List;

public interface ReagentTypeService {
    List<ReagentTypeResponse> getAllReagentType();
    // Thêm các phương thức khác liên quan đến ReagentType ở đây
}