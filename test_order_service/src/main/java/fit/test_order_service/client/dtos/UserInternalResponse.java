/*
 * @ {#} UserInternalResponse.java   1.0     07/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.test_order_service.client.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @description: Response DTO for internal user information
 * @author: Tran Hien Vinh
 * @date:   07/10/2025
 * @version:    1.0
 */
public record UserInternalResponse(
     String userId,
     String username,
     String fullName,
     String email,
     String phone,
     String address,

     LocalDate dateOfBirth,
     Integer ageYears,
     String gender,

     String identifyNumberMasked,

     String roleId,
     String roleCode,
     String roleName,

     Boolean emailVerified,

     LocalDateTime createdAt,
     LocalDateTime updatedAt,
     LocalDateTime deletedAt
){}
