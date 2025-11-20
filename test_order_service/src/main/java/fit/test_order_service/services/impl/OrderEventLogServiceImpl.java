///*
// * @ (#) OrderEventLogServiceImpl.java    1.0    13/10/2025
// * Copyright (c) 2025 IUH. All rights reserved.
// */
//package fit.test_order_service.services.impl;/*
// * @description:
// * @author: Bao Thong
// * @date: 13/10/2025
// * @version: 1.0
// */
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import fit.test_order_service.entities.OrderEventLog;
//import fit.test_order_service.entities.TestOrder;
//import fit.test_order_service.enums.EventType;
//import fit.test_order_service.repositories.OrderEventLogRepository;
//import fit.test_order_service.services.OrderEventLogService;
//import fit.test_order_service.utils.SecurityUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import fit.test_order_service.utils.RequestUtils;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//
//
//@Service
//@RequiredArgsConstructor
//public class OrderEventLogServiceImpl implements OrderEventLogService {
//
//    private final OrderEventLogRepository orderEventLogRepository;
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void logEvent(TestOrder order, EventType eventType, String details) {
//        String currentUserId = SecurityUtils.getCurrentUserId();
//        if (currentUserId == null) {
//            currentUserId = "SYSTEM";
//        }
//
//        String orderId = (order != null) ? order.getOrderId() : "SYSTEM_ORDER_ID";
//
//        OrderEventLog eventLog = OrderEventLog.builder()
//                .orderRef(order)
//                .orderId(orderId)
//                .eventType(eventType)
//                .actorUserId(currentUserId)
//                .details(details)
//                .ipAddress(RequestUtils.getIpAddress())
//                .userAgent(RequestUtils.getUserAgent())
//                .build();
//
//        orderEventLogRepository.save(eventLog);
//    }
//
//    @Override
//    public void logOrderUpdate(TestOrder before, TestOrder after, EventType type) {
//        String currentUserId = Optional.ofNullable(SecurityUtils.getCurrentUserId()).orElse("SYSTEM");
//
//        Map<String, Map<String, String>> changes = new LinkedHashMap<>();
//        compare(changes, "status", before.getStatus(), after.getStatus());
//        compare(changes, "reviewStatus", before.getReviewStatus(), after.getReviewStatus());
//        compare(changes, "reviewMode", before.getReviewMode(), after.getReviewMode());
//
//        if (changes.isEmpty()) return;
//
//        String details = "Test order updated - Changed fields: " + toJson(changes);
//        String beforeJson = toJson(snapshot(before));
//        String afterJson = toJson(snapshot(after));
//
//        OrderEventLog log = OrderEventLog.builder()
//                .orderRef(after)
//                .orderId(after.getOrderId())
//                .eventType(type)
//                .actorUserId(currentUserId)
//                .details(details)
//                .beforeJson(beforeJson)
//                .afterJson(afterJson)
//                .ipAddress(RequestUtils.getIpAddress())
//                .userAgent(RequestUtils.getUserAgent())
//                .build();
//
//        orderEventLogRepository.save(log);
//    }
//
//    // Helper method to compare values and record changes
//    private void compare(Map<String, Map<String, String>> map, String field, Object oldVal, Object newVal) {
//        if (!Objects.equals(oldVal, newVal)) {
//            map.put(field, Map.of("from", String.valueOf(oldVal), "to", String.valueOf(newVal)));
//        }
//    }
//
//    private Map<String, Object> snapshot(TestOrder order) {
//        Map<String, Object> snapshot = new LinkedHashMap<>();
//        snapshot.put("orderId", order.getOrderId());
//        snapshot.put("orderCode", order.getOrderCode());
//        snapshot.put("status", order.getStatus());
//        snapshot.put("reviewStatus", order.getReviewStatus());
//        snapshot.put("reviewMode", order.getReviewMode());
//        snapshot.put("updatedBy", order.getUpdatedBy());
//        return snapshot;
//    }
//
//    private String toJson(Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (JsonProcessingException e) {
//            return "{}";
//        }
//    }
//}
/*
 * @ (#) OrderEventLogServiceImpl.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.test_order_service.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.test_order_service.entities.OrderEventLog;
import fit.test_order_service.entities.TestOrder;
import fit.test_order_service.enums.EventType;
import fit.test_order_service.repositories.OrderEventLogRepository;
import fit.test_order_service.services.OrderEventLogService;
import fit.test_order_service.utils.RequestUtils;
import fit.test_order_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderEventLogServiceImpl implements OrderEventLogService {

    private final OrderEventLogRepository orderEventLogRepository;

    @Override
    // [FIX] Bỏ propagation = Propagation.REQUIRES_NEW để chạy chung transaction với cha
    @Transactional
    public void logEvent(TestOrder order, EventType eventType, String details) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = "SYSTEM";
        }

        String orderId = (order != null) ? order.getOrderId() : "SYSTEM_ORDER_ID";

        OrderEventLog eventLog = OrderEventLog.builder()
                .orderRef(order)
                .orderId(orderId)
                .eventType(eventType)
                .actorUserId(currentUserId)
                .details(details)
                .ipAddress(RequestUtils.getIpAddress())
                .userAgent(RequestUtils.getUserAgent())
                .build();

        orderEventLogRepository.save(eventLog);
    }

    @Override
    @Transactional // Đảm bảo update log cũng chạy trong transaction
    public void logOrderUpdate(TestOrder before, TestOrder after, EventType type) {
        String currentUserId = Optional.ofNullable(SecurityUtils.getCurrentUserId()).orElse("SYSTEM");

        Map<String, Map<String, String>> changes = new LinkedHashMap<>();
        compare(changes, "status", before.getStatus(), after.getStatus());
        compare(changes, "reviewStatus", before.getReviewStatus(), after.getReviewStatus());
        compare(changes, "reviewMode", before.getReviewMode(), after.getReviewMode());

        if (changes.isEmpty()) return;

        String details = "Test order updated - Changed fields: " + toJson(changes);
        String beforeJson = toJson(snapshot(before));
        String afterJson = toJson(snapshot(after));

        OrderEventLog log = OrderEventLog.builder()
                .orderRef(after)
                .orderId(after.getOrderId())
                .eventType(type)
                .actorUserId(currentUserId)
                .details(details)
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .ipAddress(RequestUtils.getIpAddress())
                .userAgent(RequestUtils.getUserAgent())
                .build();

        orderEventLogRepository.save(log);
    }

    // Helper method to compare values and record changes
    private void compare(Map<String, Map<String, String>> map, String field, Object oldVal, Object newVal) {
        if (!Objects.equals(oldVal, newVal)) {
            map.put(field, Map.of("from", String.valueOf(oldVal), "to", String.valueOf(newVal)));
        }
    }

    private Map<String, Object> snapshot(TestOrder order) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderId", order.getOrderId());
        snapshot.put("orderCode", order.getOrderCode());
        snapshot.put("status", order.getStatus());
        snapshot.put("reviewStatus", order.getReviewStatus());
        snapshot.put("reviewMode", order.getReviewMode());
        snapshot.put("updatedBy", order.getUpdatedBy());
        return snapshot;
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
