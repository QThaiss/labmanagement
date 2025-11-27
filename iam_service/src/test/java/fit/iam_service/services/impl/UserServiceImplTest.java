package fit.iam_service.services.impl;

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.*;
import fit.iam_service.enums.Gender;
import fit.iam_service.exceptions.AlreadyVerifiedException;
import fit.iam_service.repositories.*;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.services.OtpService;
import fit.iam_service.utils.RsaDecryptUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordHistoryRepository pwdHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private RsaDecryptUtils rsaUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;
    private String clientIp;
    private String userAgent;

    @BeforeEach
    void setUp() {
        clientIp = "127.0.0.1";
        userAgent = "Test-Agent";

        testRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_USER")
                .roleName("User")
                .build();

        testUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("testuser")
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .role(testRole)
                .isDeleted(false)
                .dateOfBirth(LocalDate.now().minusYears(25))
                .gender(Gender.MALE)
                .fullName("Test User")
                .phone("+84901234567")
                .identifyNumber("123456789")
                .address("Test Address")
                .emailVerified(false)
                .build();
    }

    @Test
    void create_Success_PublicSignup() {
        // Arrange
        LocalDate dob = LocalDate.of(1999, 1, 15);
        int age = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();

        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "new@example.com",
                "+84909999999",
                "New User",
                "987654321",
                "MALE",
                age, // Use calculated age
                "New Address",
                "01/15/1999",
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumber(anyString())).thenReturn(false);
        when(roleRepository.findByRoleCode("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(rsaUtils.decrypt(anyString())).thenReturn("ValidPass123");
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        CreateUserResponse result = userService.create(request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        verify(userRepository).saveAndFlush(any(User.class));
        verify(pwdHistoryRepository).save(any(PasswordHistory.class));
        verify(otpService).createAndSendEmailVerifyOtp(any(User.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void create_EmailAlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "existing@example.com",
                "+84909999999",
                "New User",
                "987654321",
                "MALE",
                25,
                "New Address",
                "01/15/1999",
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(request, clientIp, userAgent));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_PhoneAlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "new@example.com",
                "+84901234567",
                "New User",
                "987654321",
                "MALE",
                25,
                "New Address",
                "01/15/1999",
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(request, clientIp, userAgent));
    }

    @Test
    void create_IdentifyNumberAlreadyExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "new@example.com",
                "+84909999999",
                "New User",
                "123456789",
                "MALE",
                25,
                "New Address",
                "01/15/1999",
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumber(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(request, clientIp, userAgent));
    }

    @Test
    void create_AgeMismatch() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "new@example.com",
                "+84909999999",
                "New User",
                "987654321",
                "MALE",
                30, // Wrong age
                "New Address",
                "01/15/1999", // Should be 25 years old
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumber(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(request, clientIp, userAgent));
    }

    @Test
    void create_InvalidPassword() {
        // Arrange
        LocalDate dob = LocalDate.of(1999, 1, 15);
        int age = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();

        CreateUserRequest request = new CreateUserRequest(
                "newuser",
                "new@example.com",
                "+84909999999",
                "New User",
                "987654321",
                "MALE",
                age,
                "New Address",
                "01/15/1999",
                "encryptedPassword",
                null
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumber(anyString())).thenReturn(false);
        when(roleRepository.findByRoleCode("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(rsaUtils.decrypt(anyString())).thenReturn("weak"); // Too short

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(request, clientIp, userAgent));
    }

    @Test
    void verifyEmail_Success() {
        // Arrange
        EmailVerifyRequest request = new EmailVerifyRequest(testUser.getUserId(), "123456");

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(otpService.verifyEmailOtp(testUser, "123456")).thenReturn(true);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        EmailVerifyResponse result = userService.verifyEmail(request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.userId());
        assertTrue(result.emailVerified());
        verify(userRepository).saveAndFlush(any(User.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void verifyEmail_AlreadyVerified() {
        // Arrange
        testUser.setEmailVerified(true);
        EmailVerifyRequest request = new EmailVerifyRequest(testUser.getUserId(), "123456");

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act
        EmailVerifyResponse result = userService.verifyEmail(request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertTrue(result.emailVerified());
        verify(otpService, never()).verifyEmailOtp(any(), anyString());
    }

    @Test
    void verifyEmail_InvalidOtp() {
        // Arrange
        EmailVerifyRequest request = new EmailVerifyRequest(testUser.getUserId(), "wrong");

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(otpService.verifyEmailOtp(testUser, "wrong")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.verifyEmail(request, clientIp, userAgent));
    }

    @Test
    void verifyEmail_UserNotFound() {
        // Arrange
        EmailVerifyRequest request = new EmailVerifyRequest("non-existent", "123456");
        when(userRepository.findActiveById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> userService.verifyEmail(request, clientIp, userAgent));
    }

    @Test
    void update_Success_AsOwner() {
        // Arrange
        LocalDate dob = LocalDate.of(1999, 2, 20);
        int age = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();

        UpdateUserRequest request = new UpdateUserRequest(
                "Updated Name",
                "02/20/1999",
                age, // Use calculated age
                "FEMALE",
                "Updated Address",
                "updated@example.com",
                "+84908888888"
        );

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByPhoneAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumberAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        UpdateUserResponse result = userService.update(testUser, testUser.getUserId(), request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.fullName());
        verify(userRepository).saveAndFlush(any(User.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void update_Success_AsAdmin() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_ADMIN")
                .roleName("Admin")
                .build();

        User adminUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@example.com")
                .role(adminRole)
                .build();

        LocalDate dob = LocalDate.of(1995, 3, 15);
        int age = Period.between(dob, LocalDate.now(ZoneOffset.UTC)).getYears();

        UpdateUserRequest request = new UpdateUserRequest(
                "Updated by Admin",
                "03/15/1995",
                age, // Use calculated age
                "MALE",
                "Admin Updated Address",
                "adminupdate@example.com",
                "+84907777777"
        );

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByPhoneAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.existsByIdentifyNumberAndUserIdIsNot(anyString(), anyString())).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        UpdateUserResponse result = userService.update(adminUser, testUser.getUserId(), request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void update_AccessDenied() {
        // Arrange
        User otherUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("other")
                .email("other@example.com")
                .role(testRole)
                .build();

        UpdateUserRequest request = new UpdateUserRequest(
                "Hacker",
                "01/01/2000",
                24,
                "MALE",
                "Hack Address",
                "hack@example.com",
                "+84906666666"
        );

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.update(otherUser, testUser.getUserId(), request, clientIp, userAgent));
    }

    @Test
    void list_Success() {
        // Arrange
        UserListQuery query = new UserListQuery(
                null, null, null, null, null, null,
                0, 20, "fullName", "asc"
        );

        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        PageResult<UserListItem> result = userService.list(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
    }

    @Test
    void list_WithFilters() {
        // Arrange
        UserListQuery query = new UserListQuery(
                "test", "MALE", 20, 30,
                LocalDate.now().minusYears(30), LocalDate.now().minusYears(20),
                0, 20, "email", "desc"
        );

        Page<User> page = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        PageResult<UserListItem> result = userService.list(query);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deleteUser_Success_AsAdmin() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_ADMIN")
                .roleName("Admin")
                .build();

        User adminUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@example.com")
                .role(adminRole)
                .build();

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.softDelete(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(1);

        // Act
        DeleteUserResult result = userService.deleteUser(adminUser, testUser.getUserId());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        verify(authSessionRepository).deleteAllByUserId(testUser.getUserId());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void deleteUser_CannotDeleteSelf() {
        // Arrange
        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.deleteUser(testUser, testUser.getUserId()));
    }

    @Test
    void deleteUser_NotAuthenticated() {
        // Arrange
        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.deleteUser(null, testUser.getUserId()));
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_ADMIN")
                .roleName("Admin")
                .build();

        User adminUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@example.com")
                .role(adminRole)
                .build();

        when(userRepository.findActiveById(anyString())).thenReturn(Optional.empty());
        when(userRepository.isDeletedFlag(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> userService.deleteUser(adminUser, "non-existent"));
    }

    @Test
    void deleteUser_AlreadyDeleted() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_ADMIN")
                .roleName("Admin")
                .build();

        User adminUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@example.com")
                .role(adminRole)
                .build();

        when(userRepository.findActiveById(anyString())).thenReturn(Optional.empty());
        when(userRepository.isDeletedFlag(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(adminUser, testUser.getUserId()));
    }

    @Test
    void changePassword_Success_AsOwner() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123",
                "NewPassword456",
                "NewPassword456"
        );

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", testUser.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$10$newHash");
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        ChangePasswordResponse result = userService.changePassword(
                testUser, testUser.getUserId(), request, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.userId());
        verify(pwdHistoryRepository).save(any(PasswordHistory.class));
        verify(authSessionRepository).deleteAllByUserId(testUser.getUserId());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void changePassword_OwnerMustProvideCurrentPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
                null,
                "NewPassword456",
                "NewPassword456"
        );

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.changePassword(testUser, testUser.getUserId(), request, clientIp, userAgent));
    }

    @Test
    void changePassword_CurrentPasswordIncorrect() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPassword",
                "NewPassword456",
                "NewPassword456"
        );

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.changePassword(testUser, testUser.getUserId(), request, clientIp, userAgent));
    }

    @Test
    void changePassword_PasswordsMustNotMatch() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
                "CurrentPassword123",
                "NewPassword456",
                "NewPassword456"
        );

        when(userRepository.findActiveById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("CurrentPassword123", testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", testUser.getPasswordHash())).thenReturn(true); // Same password

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword(testUser, testUser.getUserId(), request, clientIp, userAgent));
    }

    @Test
    void viewDetail_Success_AsOwner() {
        // Arrange
        when(userRepository.findActiveByIdFetchRole(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act
        UserDetailResponse result = userService.viewDetail(testUser, testUser.getUserId(), clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void viewDetail_Success_AsAdmin() {
        // Arrange
        Role adminRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_ADMIN")
                .roleName("Admin")
                .build();

        User adminUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@example.com")
                .role(adminRole)
                .build();

        when(userRepository.findActiveByIdFetchRole(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act
        UserDetailResponse result = userService.viewDetail(adminUser, testUser.getUserId(), clientIp, userAgent);

        // Assert
        assertNotNull(result);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void viewDetail_AccessDenied() {
        // Arrange
        User otherUser = User.builder()
                .userId(UUID.randomUUID().toString())
                .username("other")
                .email("other@example.com")
                .role(testRole)
                .build();

        when(userRepository.findActiveByIdFetchRole(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> userService.viewDetail(otherUser, testUser.getUserId(), clientIp, userAgent));
    }

    @Test
    void viewDetail_UserNotFound() {
        // Arrange
        when(userRepository.findActiveByIdFetchRole(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> userService.viewDetail(testUser, "non-existent", clientIp, userAgent));
    }
}