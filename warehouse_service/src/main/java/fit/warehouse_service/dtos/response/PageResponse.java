/*
 * @ {#} PageResponse.java   1.0     27/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.warehouse_service.dtos.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/*
 * @description: Generic class for paginated responses
 * @author: Tran Hien Vinh
 * @date:   27/09/2025
 * @version:    1.0
 */
@Data
@Builder
public class PageResponse<T> {
    private List<T> values;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private List<String> sorts;

    private FilterInfo filters;

    private boolean last;

    // Convert a Spring Data Page object to a PageResponse
    public static <T> PageResponse<T> from(Page<T> page, FilterInfo filterInfo) {
        List<String> sortInfos = page.getSort().isSorted()
                ? page.getSort().stream()
                .map(order -> order.getProperty() + ": " + order.getDirection())
                .toList()
                : null;

        return PageResponse.<T>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sorts(sortInfos)
                .values(page.getContent())
                .last(page.isLast())
                .filters(filterInfo)
                .build();
    }
}
