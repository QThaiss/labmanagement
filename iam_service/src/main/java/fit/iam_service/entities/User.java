/*
 * @ (#) User.java    1.1    01/10/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package fit.iam_service.entities;

import fit.iam_service.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uq_users_identify", columnNames = "identify_number"),
                @UniqueConstraint(name = "uq_users_username", columnNames = "username")
        },
        indexes = {
                @Index(name = "idx_users_full_name", columnList = "full_name"),
                @Index(name = "idx_users_dob", columnList = "date_of_birth"),
                @Index(name = "idx_users_username", columnList = "username"),
                @Index(name = "idx_users_locked_until", columnList = "locked_until"),
                @Index(name = "idx_users_last_login_at", columnList = "last_login_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = NOW() WHERE user_id = ?")
//@Where(clause = "is_deleted = false")
public class  User {

    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String userId;

    // NEW: username (đăng nhập bằng username hoặc email)
    @Size(max = 64)
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,64}$",
            message = "Username chỉ gồm chữ, số, dấu chấm, gạch dưới, gạch nối (3-64 ký tự)")
    @Column(name = "username", length = 64)
    private String username;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email là bắt buộc")
    @Size(max = 255)
    @Column(name = "email", length = 255, nullable = false)
    private String email;

    // E.164
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Số điện thoại không hợp lệ (E.164)")
    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @NotBlank(message = "Họ tên là bắt buộc")
    @Size(max = 150)
    @Column(name = "full_name", length = 150, nullable = false)
    private String fullName;

    @Pattern(regexp = "^\\d{8,20}$", message = "Số định danh không hợp lệ")
    @Size(max = 50)
    @Column(name = "identify_number", length = 50, nullable = false)
    private String identifyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10, nullable = false)
    private Gender gender;

    @NotNull(message = "Ngày sinh là bắt buộc")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // Tính tuổi (không lưu thật)
    @Formula("TIMESTAMPDIFF(YEAR, date_of_birth, CURDATE())")
    private Integer ageYears;

    @Size(max = 255)
    @Column(name = "address", length = 255)
    private String address;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 8, max = 128, message = "Mật khẩu từ 8-128 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,128}$",
            message = "Mật khẩu phải có cả chữ và số")
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "password_changed_at", nullable = false)
    private LocalDateTime passwordChangedAt;

    // NEW: thông tin đăng nhập gần nhất
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "last_login_user_agent", length = 255)
    private String lastLoginUserAgent;

    // NEW: chống brute force / lockout
    @Builder.Default
    @Column(name = "failed_login_count", nullable = false)
    private Integer failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Builder.Default
    private Boolean emailVerified = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_role"))
    private Role role;

    @PrePersist
    void prePersist() {
        if (this.userId == null) this.userId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (this.createdAt == null) this.createdAt = now;
        if (this.passwordChangedAt == null) this.passwordChangedAt = now;
        if (this.gender == null) this.gender = Gender.MALE; // tuỳ default của bạn
        if (this.failedLoginCount == null) this.failedLoginCount = 0;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}