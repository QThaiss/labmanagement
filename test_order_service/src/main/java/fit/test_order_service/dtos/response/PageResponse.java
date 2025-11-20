/*
 * @ {#} PageResponse.java   1.0     14/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

/*
 * @description: Filter information for querying orders
 * @author: Tran Hien Vinh
 * @date:   14/10/2025
 * @version:    1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> values;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<String> sorts;
    private FilterInfo filters;
    private String message;
    private boolean last;

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

    public static <T> PageResponse<T> empty(int page, int size, String message, FilterInfo filterInfo) {
        Page<T> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0);
        PageResponse<T> resp = from(emptyPage, filterInfo);
        resp.setMessage(message);
        return resp;
    }
}
