package fit.iam_service.services.implTest;

import fit.iam_service.entities.AuthSession;
import fit.iam_service.entities.Role;
import fit.iam_service.entities.User;
import fit.iam_service.enums.Gender;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.repositories.AuthSessionRepository;
import fit.iam_service.services.impl.AuthSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceImplTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @InjectMocks
    private AuthSessionServiceImpl authSessionService;

    private User testUser;
    private Role testRole;
    private String jti;
    private String refreshTokenHash;
    private String clientIp;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
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

        jti = "test-jti-" + UUID.randomUUID();
        refreshTokenHash = "$2a$10$hashedRefreshToken";
        clientIp = "127.0.0.1";
        expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusDays(7);
    }

    @Test
    void createSession_Success_NoExistingSessions() {
        // Arrange
        when(authSessionRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfter(
                eq(testUser), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AuthSession result = authSessionService.createSession(testUser, jti, refreshTokenHash, clientIp, expiresAt);

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(jti, result.getJti());
        assertEquals(refreshTokenHash, result.getRefreshTokenHash());
        assertEquals(clientIp, result.getIp());
        assertEquals(expiresAt, result.getExpiresAt());

        verify(authSessionRepository).deleteExpiredAndRevokedSessions(any(), any());
        verify(authSessionRepository).save(any(AuthSession.class));
    }

    @Test
    void createSession_Success_WithExistingSessions() {
        // Arrange
        List<AuthSession> existingSessions = new ArrayList<>();

        AuthSession session1 = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("jti-1")
                .issuedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(5))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1))
                .build();

        AuthSession session2 = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("jti-2")
                .issuedAt(LocalDateTime.now(ZoneOffset.UTC).minusHours(3))
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(2))
                .build();

        existingSessions.add(session1);
        existingSessions.add(session2);

        when(authSessionRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfter(
                eq(testUser), any(LocalDateTime.class)))
                .thenReturn(existingSessions);
        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AuthSession result = authSessionService.createSession(testUser, jti, refreshTokenHash, clientIp, expiresAt);

        // Assert
        assertNotNull(result);
        verify(authSessionRepository).save(any(AuthSession.class));
        verify(authSessionRepository, never()).save(argThat(session ->
                session.getRevokedAt() != null
        ));
    }

    @Test
    void createSession_MaxSessionsReached_RevokesOldest() {
        // Arrange
        List<AuthSession> existingSessions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Create 3 existing sessions (max sessions)
        for (int i = 0; i < 3; i++) {
            AuthSession session = AuthSession.builder()
                    .sessionId(UUID.randomUUID().toString())
                    .user(testUser)
                    .jti("jti-" + i)
                    .issuedAt(now.minusHours(i + 1))
                    .expiresAt(now.plusDays(i + 1))
                    .build();
            existingSessions.add(session);
        }

        when(authSessionRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfter(
                eq(testUser), any(LocalDateTime.class)))
                .thenReturn(existingSessions);
        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AuthSession result = authSessionService.createSession(testUser, jti, refreshTokenHash, clientIp, expiresAt);

        // Assert
        assertNotNull(result);

        // Verify that the oldest session was revoked
        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionRepository, atLeastOnce()).save(sessionCaptor.capture());

        List<AuthSession> savedSessions = sessionCaptor.getAllValues();
        boolean hasRevokedSession = savedSessions.stream()
                .anyMatch(s -> s.getRevokedAt() != null);
        assertTrue(hasRevokedSession);
    }

    @Test
    void revokeSession_Success() {
        // Arrange
        String jtiToRevoke = "jti-to-revoke";

        // Act
        authSessionService.revokeSession(jtiToRevoke);

        // Assert
        verify(authSessionRepository).revokeSessionByJti(eq(jtiToRevoke), any(LocalDateTime.class));
    }

    @Test
    void revokeAllUserSessions_Success() {
        // Arrange
        // Act
        authSessionService.revokeAllUserSessions(testUser);

        // Assert
        verify(authSessionRepository).revokeAllActiveSessionsByUser(eq(testUser), any(LocalDateTime.class));
    }

    @Test
    void findActiveSessionByJti_Success() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AuthSession activeSession = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(now)
                .expiresAt(now.plusDays(7))
                .revokedAt(null)
                .build();

        when(authSessionRepository.findByJti(jti)).thenReturn(Optional.of(activeSession));

        // Act
        Optional<AuthSession> result = authSessionService.findActiveSessionByJti(jti);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(jti, result.get().getJti());
        verify(authSessionRepository).findByJti(jti);
    }

    @Test
    void findActiveSessionByJti_NotFound() {
        // Arrange
        when(authSessionRepository.findByJti(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<AuthSession> result = authSessionService.findActiveSessionByJti("non-existent-jti");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findActiveSessionByJti_Expired() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AuthSession expiredSession = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(now.minusDays(8))
                .expiresAt(now.minusDays(1)) // Expired
                .revokedAt(null)
                .build();

        when(authSessionRepository.findByJti(jti)).thenReturn(Optional.of(expiredSession));

        // Act
        Optional<AuthSession> result = authSessionService.findActiveSessionByJti(jti);

        // Assert
        assertFalse(result.isPresent()); // Should not return expired session
    }

    @Test
    void findActiveSessionByJti_Revoked() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        AuthSession revokedSession = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .issuedAt(now)
                .expiresAt(now.plusDays(7))
                .revokedAt(now.minusHours(1)) // Revoked
                .build();

        when(authSessionRepository.findByJti(jti)).thenReturn(Optional.of(revokedSession));

        // Act
        Optional<AuthSession> result = authSessionService.findActiveSessionByJti(jti);

        // Assert
        assertFalse(result.isPresent()); // Should not return revoked session
    }

    @Test
    void cleanupExpiredSessions_Success() {
        // Arrange
        // Act
        authSessionService.cleanupExpiredSessions();

        // Assert
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(authSessionRepository).deleteExpiredAndRevokedSessions(
                nowCaptor.capture(),
                cutoffCaptor.capture()
        );

        LocalDateTime now = nowCaptor.getValue();
        LocalDateTime cutoff = cutoffCaptor.getValue();

        assertNotNull(now);
        assertNotNull(cutoff);
        assertTrue(cutoff.isBefore(now));
    }

    @Test
    void findByJti_Success() {
        // Arrange
        AuthSession session = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .build();

        when(authSessionRepository.findByJti(jti)).thenReturn(Optional.of(session));

        // Act
        AuthSession result = authSessionService.findByJti(jti);

        // Assert
        assertNotNull(result);
        assertEquals(jti, result.getJti());
        verify(authSessionRepository).findByJti(jti);
    }

    @Test
    void findByJti_NotFound() {
        // Arrange
        when(authSessionRepository.findByJti(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authSessionService.findByJti("non-existent-jti"));
    }

    @Test
    void revoke_Success() {
        // Arrange
        AuthSession session = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti(jti)
                .refreshTokenHash(refreshTokenHash)
                .build();

        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authSessionService.revoke(session);

        // Assert
        assertNotNull(session.getRevokedAt());
        verify(authSessionRepository).save(session);
    }

    @Test
    void revokeAllForUser_Success() {
        // Arrange
        // Act
        authSessionService.revokeAllForUser(testUser);

        // Assert
        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(authSessionRepository).revokeAllActiveSessionsByUser(
                eq(testUser),
                timeCaptor.capture()
        );

        assertNotNull(timeCaptor.getValue());
    }

    @Test
    void createSession_DeletesExpiredAndRevokedBeforeCreating() {
        // Arrange
        when(authSessionRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfter(
                eq(testUser), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());
        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authSessionService.createSession(testUser, jti, refreshTokenHash, clientIp, expiresAt);

        // Assert
        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(authSessionRepository).deleteExpiredAndRevokedSessions(
                nowCaptor.capture(),
                nowCaptor.capture()
        );

        assertNotNull(nowCaptor.getValue());
    }

    @Test
    void createSession_WithMultipleSessions_RevokesCorrectOne() {
        // Arrange
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        AuthSession oldest = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("jti-oldest")
                .issuedAt(now.minusHours(10))
                .expiresAt(now.plusDays(1))
                .build();

        AuthSession middle = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("jti-middle")
                .issuedAt(now.minusHours(5))
                .expiresAt(now.plusDays(2))
                .build();

        AuthSession newest = AuthSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .user(testUser)
                .jti("jti-newest")
                .issuedAt(now.minusHours(1))
                .expiresAt(now.plusDays(3))
                .build();

        List<AuthSession> sessions = new ArrayList<>();
        sessions.add(middle);
        sessions.add(newest);
        sessions.add(oldest);

        when(authSessionRepository.findByUserAndRevokedAtIsNullAndExpiresAtAfter(
                eq(testUser), any(LocalDateTime.class)))
                .thenReturn(sessions);
        when(authSessionRepository.save(any(AuthSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authSessionService.createSession(testUser, jti, refreshTokenHash, clientIp, expiresAt);

        // Assert
        verify(authSessionRepository, atLeastOnce()).save(argThat(session ->
                session.getJti().equals("jti-oldest") && session.getRevokedAt() != null
        ));
    }
}