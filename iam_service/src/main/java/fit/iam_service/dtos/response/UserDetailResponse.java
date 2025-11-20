/*
 * @ (#) UserDetailResponse.java    1.0    06/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.dtos.response;/*
 * @description:
 * @author: Bao Thong
 * @date: 06/10/2025
 * @version: 1.0
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import fit.iam_service.enums.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponse {
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;

    private LocalDate dateOfBirth;
    private Integer ageYears;
    private Gender gender;

    private String identifyNumberMasked;

    private String roleId;
    private String roleCode;
    private String roleName;

    private Boolean emailVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
