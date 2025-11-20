/*
 * @ {#} SortUtils.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.utils;

import fit.test_order_service.exceptions.InvalidSortFieldException;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * @description: Utility class for building Sort objects from query parameters
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@UtilityClass
public class SortUtils {
    public Sort buildSort(String[] sort, Set<String> allowedFields, String defaultSortField) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sort != null && sort.length > 0) {
            if (sort.length == 2 && !sort[1].contains(",") &&
                    ("asc".equalsIgnoreCase(sort[1]) || "desc".equalsIgnoreCase(sort[1]))) {
                String sortParam = sort[0] + "," + sort[1];
                processSort(sortParam, orders, allowedFields);
            } else {
                for (String param : sort) {
                    processSort(param, orders, allowedFields);
                }
            }
        }

        return orders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, defaultSortField)
                : Sort.by(orders);
    }

    private void processSort(String sortParam, List<Sort.Order> orders, Set<String> allowedFields) {
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();

        if (!allowedFields.contains(property)) {
            throw new InvalidSortFieldException(
                    String.format("Sort field '%s' is not supported. Allowed fields are: %s", property, allowedFields)
            );
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String directionStr = parts[1].trim().toLowerCase();
            if ("desc".equals(directionStr)) {
                direction = Sort.Direction.DESC;
            }
        }

        orders.add(new Sort.Order(direction, property));
    }
}
