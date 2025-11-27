package fit.iam_service.services.impl;

import fit.iam_service.dtos.request.*;
import fit.iam_service.dtos.response.*;
import fit.iam_service.entities.*;
import fit.iam_service.enums.Gender;
import fit.iam_service.exceptions.*;
import fit.iam_service.mappers.AuthMapper;
import fit.iam_service.repositories.*;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.security.jwt.JwtUtils;
import fit.iam_service.services.*;
import fit.iam_service.utils.HashRefreshToken;
import fit.iam_service.validators.AuthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PasswordResetRequestRepository passwordResetRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;
    private String clientIp;

    @BeforeEach
    void setUp() {
        clientIp = "127.0.0.1";

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
                .build();

        loginRequest = new LoginRequest("testuser", "encryptedPassword");
    }

    @Test
    void login_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(testUser.getUserId())
                .username(testUser.getUsername())
                .password(testUser.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(jwtUtils.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refreshToken");
        when(jwtUtils.getJtiFromToken(anyString())).thenReturn("jti-123");
        when(jwtUtils.getExpirationFromToken(anyString())).thenReturn(LocalDateTime.now().plusDays(1));

        LoginResponse expectedResponse = new LoginResponse(
                testUser.getUserId(),
                testUser.getUsername(),
                "ROLE_USER",
                List.of(),
                "accessToken",
                "refreshToken"
        );
        when(authMapper.mapToLoginResponse(any(), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(expectedResponse);

        // Act
        LoginResponse result = authService.login(loginRequest, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.userId());
        assertEquals("accessToken", result.accessToken());
        assertEquals("refreshToken", result.refreshToken());
        verify(authValidator).validateLoginRequest(loginRequest);
        verify(authSessionService).createSession(any(), anyString(), anyString(), anyString(), any());
        verify(auditLogService).logUserLogin(any(), anyString());
        verify(userRepository).save(testUser);
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("oldRefreshToken");
        AuthSession authSession = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("old-jti")
                .refreshTokenHash(HashRefreshToken.hashRefreshToken("oldRefreshToken"))
                .build();

        RefreshTokenResponse expectedResponse = new RefreshTokenResponse("newAccessToken", "newRefreshToken");

        when(authValidator.validateRefreshToken(anyString())).thenReturn(authSession);
        when(authMapper.buildRefreshTokenResponse(any(), anyString())).thenReturn(expectedResponse);
        when(jwtUtils.getJtiFromToken(anyString())).thenReturn("new-jti");

        // Act
        RefreshTokenResponse result = authService.refreshToken(request, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals("newAccessToken", result.accessToken());
        assertEquals("newRefreshToken", result.refreshToken());
        verify(authSessionService).revoke(authSession);
        verify(auditLogService).logRefreshToken(any(), anyString(), eq("old-jti"), eq("new-jti"));
    }

    @Test
    void forgotPassword_Success() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        ForgotPasswordResponse expectedResponse = new ForgotPasswordResponse("Password reset email has been sent.");
        when(authMapper.buildForgotResponse()).thenReturn(expectedResponse);

        // Act
        ForgotPasswordResponse result = authService.forgotPassword(request, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals("Password reset email has been sent.", result.message());
        verify(passwordResetRequestRepository).save(any(PasswordResetRequest.class));
        verify(emailService).sendPasswordResetEmail(eq(testUser), anyString());
        verify(auditLogService).logForgotPassword(testUser, clientIp);
    }

    @Test
    void forgotPassword_UserNotFound() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest("notfound@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> authService.forgotPassword(request, clientIp));
        verify(emailService, never()).sendPasswordResetEmail(any(), anyString());
    }

    @Test
    void forgotPassword_DeletedUser() {
        // Arrange
        testUser.setDeleted(true);
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> authService.forgotPassword(request, clientIp));
    }

    @Test
    void validatePasswordResetToken_Success() {
        // Arrange
        String token = "valid-token";
        String tokenHash = HashRefreshToken.hashRefreshToken(token);
        PasswordResetRequest prr = PasswordResetRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .build();

        when(passwordResetRequestRepository.findActiveByHash(eq(tokenHash), any()))
                .thenReturn(Optional.of(prr));

        // Act
        PasswordResetRequest result = authService.validatePasswordResetToken(token);

        // Assert
        assertNotNull(result);
        assertEquals(prr.getRequestId(), result.getRequestId());
    }

    @Test
    void validatePasswordResetToken_InvalidToken() {
        // Arrange
        String token = "invalid-token";
        when(passwordResetRequestRepository.findActiveByHash(anyString(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidTokenException.class,
                () -> authService.validatePasswordResetToken(token));
    }

    @Test
    void validatePasswordResetToken_AlreadyUsed() {
        // Arrange
        String token = "used-token";
        PasswordResetRequest prr = PasswordResetRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(testUser)
                .tokenHash(HashRefreshToken.hashRefreshToken(token))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .usedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(passwordResetRequestRepository.findActiveByHash(anyString(), any()))
                .thenReturn(Optional.of(prr));

        // Act & Assert
        assertThrows(InvalidTokenException.class,
                () -> authService.validatePasswordResetToken(token));
    }

    @Test
    void validatePasswordResetToken_Expired() {
        // Arrange
        String token = "expired-token";
        PasswordResetRequest prr = PasswordResetRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(testUser)
                .tokenHash(HashRefreshToken.hashRefreshToken(token))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1))
                .build();

        when(passwordResetRequestRepository.findActiveByHash(anyString(), any()))
                .thenReturn(Optional.of(prr));

        // Act & Assert
        assertThrows(InvalidTokenException.class,
                () -> authService.validatePasswordResetToken(token));
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String token = "reset-token";
        ResetPasswordRequest request = new ResetPasswordRequest(token, "NewPassword123", "NewPassword123");

        PasswordResetRequest prr = PasswordResetRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .user(testUser)
                .tokenHash(HashRefreshToken.hashRefreshToken(token))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15))
                .build();

        when(passwordResetRequestRepository.findActiveByHash(anyString(), any()))
                .thenReturn(Optional.of(prr));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$newHashedPassword");

        ResetPasswordResponse expectedResponse = new ResetPasswordResponse("Password has been reset successfully.");
        when(authMapper.buildResetResponse()).thenReturn(expectedResponse);

        // Act
        ResetPasswordResponse result = authService.resetPassword(request, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals("Password has been reset successfully.", result.message());
        verify(authValidator).validateResetRequest(request);
        verify(authValidator).ensurePasswordNotReused(testUser, "NewPassword123");
        verify(userRepository).save(testUser);
        verify(passwordHistoryRepository).save(any(PasswordHistory.class));
        verify(authSessionService).revokeAllForUser(testUser);
        verify(auditLogService).logPasswordReset(testUser, clientIp);
    }

    @Test
    void logoutCurrent_Success() {
        // Arrange
        LogoutRequest request = new LogoutRequest("refreshToken");
        AuthSession session = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("session-jti")
                .build();

        UserDetailsImpl principal = UserDetailsImpl.builder()
                .id(testUser.getUserId())
                .username(testUser.getUsername())
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authValidator.validateRefreshToken(anyString())).thenReturn(session);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);

        // Act
        LogoutResponse result = authService.logoutCurrent(request, clientIp);

        // Assert
        assertNotNull(result);
        assertEquals("Logged out current session", result.message());
        verify(authSessionService).revoke(session);
        verify(auditLogService).logUserLogout(session.getUser(), clientIp, session.getJti(), false);
    }

    @Test
    void logoutCurrent_NotAuthenticated() {
        // Arrange
        LogoutRequest request = new LogoutRequest("refreshToken");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        AuthSession session = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("session-jti")
                .build();
        when(authValidator.validateRefreshToken(anyString())).thenReturn(session);

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> authService.logoutCurrent(request, clientIp));
    }

    @Test
    void logoutAll_Success() {
        // Arrange
        UserDetailsImpl principal = UserDetailsImpl.builder()
                .id(testUser.getUserId())
                .username(testUser.getUsername())
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        SecurityContextHolder.setContext(securityContext);

        // Act
        LogoutResponse result = authService.logoutAll(clientIp);

        // Assert
        assertNotNull(result);
        assertEquals("Logged out all sessions", result.message());
        verify(authSessionService).revokeAllForUser(testUser);
        verify(auditLogService).logUserLogout(testUser, clientIp, null, true);
    }

    @Test
    void logoutAll_NotAuthenticated() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(UnauthorizedException.class,
                () -> authService.logoutAll(clientIp));
    }

    @Test
    void logoutAll_UserNotFound() {
        // Arrange
        UserDetailsImpl principal = UserDetailsImpl.builder()
                .id("non-existent-id")
                .username("testuser")
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authService.logoutAll(clientIp));
    }
}