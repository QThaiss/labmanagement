/*
 * @ {#} TestOrderStatusServiceImpl.java   1.0     23/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.services.impl;

import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.entities.TestResult;
import fit.test_order_service.enums.EventType;
import fit.test_order_service.enums.ItemStatus;
import fit.test_order_service.enums.OrderStatus;
import fit.test_order_service.exceptions.NotFoundException;
import fit.test_order_service.repositories.TestOrderRepository;
import fit.test_order_service.services.OrderEventLogService;
import fit.test_order_service.services.TestOrderStatusService;
import fit.test_order_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/*
 * @description: Implementation of TestOrderStatusService
 * @author: Tran Hien Vinh
 * @date:   23/10/2025
 * @version:    1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TestOrderStatusServiceImpl implements TestOrderStatusService {
    private final TestOrderRepository testOrderRepository;

    private final OrderEventLogService orderEventLogService;

    @Override
    public void updateOrderStatusIfNeeded(String orderId) {
        TestOrder order = testOrderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = determineOrderStatus(order);

        if (currentStatus != newStatus) {
            OrderStatus previousStatus = order.getStatus();
            order.setStatus(newStatus);
            order.setUpdatedBy(SecurityUtils.getCurrentUserId());
            order.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            order.setRunBy(SecurityUtils.getCurrentUserId());
            order.setRunAt(LocalDateTime.now(ZoneOffset.UTC));

            testOrderRepository.save(order);

            String logMessage = String.format(
                    "Order status auto-updated from %s to %s based on result completion",
                    previousStatus, newStatus
            );
            orderEventLogService.logEvent(order, EventType.UPDATE, logMessage);

            log.info("Order {} status updated from {} to {}", orderId, previousStatus, newStatus);
        }
    }

    private OrderStatus determineOrderStatus(TestOrder order) {
        List<TestResult> results = order.getResults();
        if (results == null || results.isEmpty()) {
            return OrderStatus.PENDING;
        }

        // Nếu đã có kết quả thì xem như đã hoàn tất
        return OrderStatus.COMPLETED;
    }
}
