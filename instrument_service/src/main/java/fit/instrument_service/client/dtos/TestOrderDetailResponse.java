/*
 * @ (#) TestOrderDetailResponse.java    1.0    13/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.instrument_service.client.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 13/10/2025
 * @version: 1.0
 */

import fit.instrument_service.client.dtos.enums.Gender;
import fit.instrument_service.client.dtos.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderDetailResponse {
    private String id;
    private String orderCode;
    private String medicalRecordId;
    private String medicalRecordCode;
    private String fullName;
    private Integer age;
    private Gender gender;
    private String phone;
    private String address;
    private String email;
    private String dateOfBirth;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime runAt;
    private String runBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private List<TestOrderItemResponse> items;
}