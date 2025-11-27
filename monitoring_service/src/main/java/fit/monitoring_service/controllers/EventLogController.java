/*
 * @ (#) EventLogController.java    1.0    26/11/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.monitoring_service.controllers;/*
 * @description:
 * @author: Bao Thong
 * @date: 26/11/2025
 * @version: 1.0
 */

import fit.monitoring_service.dtos.request.EventLogFilterRequest;
import fit.monitoring_service.dtos.response.ApiResponse;
import fit.monitoring_service.dtos.response.PageResponse;
import fit.monitoring_service.entities.EventLog;
import fit.monitoring_service.services.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/monitoring/event-logs")
@RequiredArgsConstructor
public class EventLogController {

    private final EventLogService eventLogService;

    /**
     * 3.2.1.2 View List Event Logs [cite: 442]
     * API này cho phép xem danh sách log, hỗ trợ filter và sort.
     * Mặc định sort theo created_at DESC (mới nhất trước).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<EventLog>>> getEventLogs(
            @ModelAttribute EventLogFilterRequest filterRequest,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        PageResponse<EventLog> result = eventLogService.getEventLogs(filterRequest, pageable);

        // Xử lý trường hợp "No Data" theo SRS
        // Thường API vẫn trả về 200 OK với list rỗng, Client sẽ hiển thị text "No Data".
        // Tuy nhiên, ta có thể thêm message vào ApiResponse.
        String message = result.getContent().isEmpty() ? "No Data" : "Get event logs successfully";

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<EventLog>>builder()
                        .status(200)
                        .success(true)
                        .message(message)
                        .data(result)
                        .build()
        );
    }

    /**
     * 3.2.1.3 View Event Log's Detail
     * API lấy chi tiết một Event Log theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<EventLog>> getEventLogDetail(@PathVariable String id) {
        EventLog eventLog = eventLogService.getEventLogById(id);

        return ResponseEntity.ok(
                ApiResponse.<EventLog>builder()
                        .status(200)
                        .success(true)
                        .message("Get event log detail successfully")
                        .data(eventLog)
                        .build()
        );
    }
}
