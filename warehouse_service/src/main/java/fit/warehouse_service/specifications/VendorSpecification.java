package fit.warehouse_service.specifications;

import fit.warehouse_service.entities.Vendor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class VendorSpecification {
    public Specification<Vendor> build(String search) {
        // Luôn loại bỏ các bản ghi đã xóa mềm
        Specification<Vendor> spec = (root, query, cb) -> cb.equal(root.get("isDeleted"), false);

        if (StringUtils.hasText(search)) {
            String searchLower = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), searchLower),
                            cb.like(cb.lower(root.get("email")), searchLower),
                            cb.like(cb.lower(root.get("contactPerson")), searchLower)
                    ));
        }
        return spec;
    }
}