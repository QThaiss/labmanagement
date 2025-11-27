package fit.warehouse_service.services.impl;

import fit.warehouse_service.dtos.response.ReagentTypeResponse;
import fit.warehouse_service.entities.ReagentType;
import fit.warehouse_service.mappers.ReagentTypeMapper;
import fit.warehouse_service.repositories.ReagentTypeRepository;
import fit.warehouse_service.services.ReagentTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentTypeServiceImpl implements ReagentTypeService {

    private final ReagentTypeRepository reagentTypeRepository;
    private final ReagentTypeMapper reagentTypeMapper; // Đã thêm ở bước 2

    // Phương thức bạn yêu cầu
    @Override
    @Transactional(readOnly = true)
    public List<ReagentTypeResponse> getAllReagentType() {
        log.info("Fetching all reagent types.");

        // 1. Lấy tất cả các entity ReagentType từ cơ sở dữ liệu
        // JpaRepository (được kế thừa bởi ReagentTypeRepository) cung cấp phương thức findAll()
        List<ReagentType> reagentTypes = reagentTypeRepository.findAll();

        log.info("Retrieved {} reagent types.", reagentTypes.size());

        // 2. Map danh sách entities sang danh sách DTOs
        return reagentTypes.stream()
                .map(reagentTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Thêm các phương thức khác liên quan đến ReagentType ở đây
}