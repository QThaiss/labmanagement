package fit.iam_service.services.implTest;

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.dtos.request.RoleListQuery;
import fit.iam_service.dtos.request.UpdateRoleRequest;
import fit.iam_service.dtos.response.CreateRoleResponse;
import fit.iam_service.dtos.response.DeleteRoleResponse;
import fit.iam_service.dtos.response.RoleListItem;
import fit.iam_service.dtos.response.UpdateRoleResponse;
import fit.iam_service.entities.AuditLog;
import fit.iam_service.entities.Privilege;
import fit.iam_service.entities.Role;
import fit.iam_service.entities.RolePrivilege;
import fit.iam_service.enums.PrivilegeCode;
import fit.iam_service.exceptions.AlreadyExistsException;
import fit.iam_service.exceptions.InvalidFormatException;
import fit.iam_service.exceptions.NotFoundException;
import fit.iam_service.mappers.RoleMapper;
import fit.iam_service.repositories.AuditLogRepository;
import fit.iam_service.repositories.PrivilegeRepository;
import fit.iam_service.repositories.RoleRepository;
import fit.iam_service.repositories.UserRepository;
import fit.iam_service.services.impl.RoleServiceImpl;
import fit.iam_service.utils.SecurityUtils;
import fit.iam_service.validators.RoleValidator;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @Mock
    private RoleValidator roleValidator;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private Privilege testPrivilege;
    private String actorId;
    private String clientIp;
    private String userAgent;

    @BeforeEach
    void setUp() {
        actorId = UUID.randomUUID().toString();
        clientIp = "127.0.0.1";
        userAgent = "Test-Agent";

        testPrivilege = Privilege.builder()
                .privilegeId(UUID.randomUUID().toString())
                .privilegeCode(PrivilegeCode.READ_ONLY)
                .privilegeName("Read Only")
                .privilegeDescription("Default read-only privilege")
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        testRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_TEST")
                .roleName("Test Role")
                .roleDescription("Test Role Description")
                .isSystem(false)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .rolePrivileges(new LinkedHashSet<>())
                .build();

        RolePrivilege rp = RolePrivilege.builder()
                .role(testRole)
                .privilege(testPrivilege)
                .build();
        testRole.getRolePrivileges().add(rp);
    }

    @Test
    void createRole_Success() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("New Role")
                .roleCode("ROLE_NEW")
                .roleDescription("New Role Description")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        Role newRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_NEW")
                .roleName("New Role")
                .roleDescription("New Role Description")
                .rolePrivileges(new LinkedHashSet<>())
                .build();

        CreateRoleResponse expectedResponse = CreateRoleResponse.builder()
                .roleId(newRole.getRoleId())
                .roleCode("ROLE_NEW")
                .roleName("New Role")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(any(PrivilegeCode.class)))
                .thenReturn(Optional.of(testPrivilege));
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        when(roleMapper.toEntity(any(CreateRoleRequest.class), anyString())).thenReturn(newRole);
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(expectedResponse);

        // Act
        CreateRoleResponse result = roleService.createRole(request, actorId, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals("ROLE_NEW", result.getRoleCode());
        assertEquals("New Role", result.getRoleName());
        verify(roleValidator).validateCreateRole(request);
        verify(roleRepository).save(any(Role.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void createRole_WithDefaultPrivileges() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("Default Role")
                .roleCode("ROLE_DEFAULT")
                .roleDescription("Role with default privileges")
                .privilegeCodes(null) // Should default to READ_ONLY
                .build();

        Role newRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_DEFAULT")
                .roleName("Default Role")
                .rolePrivileges(new LinkedHashSet<>())
                .build();

        CreateRoleResponse expectedResponse = CreateRoleResponse.builder()
                .roleId(newRole.getRoleId())
                .roleCode("ROLE_DEFAULT")
                .roleName("Default Role")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(any(PrivilegeCode.class)))
                .thenReturn(Optional.of(testPrivilege));
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        when(roleMapper.toEntity(any(CreateRoleRequest.class), anyString())).thenReturn(newRole);
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(roleMapper.toResponse(any(Role.class))).thenReturn(expectedResponse);

        // Act
        CreateRoleResponse result = roleService.createRole(request, actorId, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        verify(privilegeRepository).findByPrivilegeCodeAndIsDeletedFalse(PrivilegeCode.READ_ONLY);
    }

    @Test
    void createRole_RoleNameAlreadyExists() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("Existing Role")
                .roleCode("ROLE_EXISTING")
                .roleDescription("Description")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> roleService.createRole(request, actorId, clientIp, userAgent));
        verify(roleRepository, never()).save(any());
    }

    @Test
    void createRole_RoleCodeAlreadyExists() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("New Role")
                .roleCode("ROLE_EXISTING")
                .roleDescription("Description")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> roleService.createRole(request, actorId, clientIp, userAgent));
    }

    @Test
    void createRole_InvalidPrivilegeCode() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("New Role")
                .roleCode("ROLE_NEW")
                .roleDescription("Description")
                .privilegeCodes(List.of("INVALID_PRIVILEGE"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidFormatException.class,
                () -> roleService.createRole(request, actorId, clientIp, userAgent));
    }

    @Test
    void createRole_PrivilegeNotFound() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("New Role")
                .roleCode("ROLE_NEW")
                .roleDescription("Description")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(any(PrivilegeCode.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> roleService.createRole(request, actorId, clientIp, userAgent));
    }

    @Test
    void createRole_DuplicatePrivilegeSet() {
        // Arrange
        CreateRoleRequest request = CreateRoleRequest.builder()
                .roleName("Duplicate Role")
                .roleCode("ROLE_DUPLICATE")
                .roleDescription("Description")
                .privilegeCodes(List.of("READ_ONLY"))
                .build();

        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(any(PrivilegeCode.class)))
                .thenReturn(Optional.of(testPrivilege));
        when(roleRepository.findAll()).thenReturn(List.of(testRole)); // Role with same privileges

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> roleService.createRole(request, actorId, clientIp, userAgent));
    }

    @Test
    void updateRole_Success() {
        // Arrange
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getRoleName()).thenReturn("Updated Role");
        when(request.getRoleDescription()).thenReturn("Updated Description");
        when(request.getPrivilegeCodes()).thenReturn(List.of("READ_ONLY", "USER_VIEW"));

        Privilege viewPrivilege = Privilege.builder()
                .privilegeId(UUID.randomUUID().toString())
                .privilegeCode(PrivilegeCode.USER_VIEW)
                .privilegeName("User View")
                .isDeleted(false)
                .build();

        UpdateRoleResponse expectedResponse = UpdateRoleResponse.builder()
                .roleId(testRole.getRoleId())
                .roleCode("ROLE_TEST")
                .roleName("Updated Role")
                .roleDescription("Updated Description")
                .privilegeCodes(List.of("READ_ONLY", "USER_VIEW"))
                .build();

        when(roleRepository.findByRoleIdAndIsDeletedFalse(testRole.getRoleId()))
                .thenReturn(Optional.of(testRole));
        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(eq(PrivilegeCode.READ_ONLY)))
                .thenReturn(Optional.of(testPrivilege));
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(eq(PrivilegeCode.USER_VIEW)))
                .thenReturn(Optional.of(viewPrivilege));
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        when(roleRepository.saveAndFlush(any(Role.class))).thenReturn(testRole);
        when(roleMapper.toUpdateResponse(any(Role.class))).thenReturn(expectedResponse);

        // Act
        UpdateRoleResponse result = roleService.updateRole(
                testRole.getRoleId(), request, actorId, clientIp, userAgent);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Role", result.getRoleName());
        verify(roleMapper).updateEntity(eq(request), eq(testRole), eq(actorId), any());
        verify(roleRepository).saveAndFlush(testRole);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void updateRole_RoleNotFound() {
        // Arrange
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getRoleName()).thenReturn("Updated Role");

        when(roleRepository.findByRoleIdAndIsDeletedFalse(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> roleService.updateRole("non-existent", request, actorId, clientIp, userAgent));
    }

    @Test
    void updateRole_SystemRole() {
        // Arrange
        testRole.setSystem(true);
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getRoleName()).thenReturn("Updated Role");

        when(roleRepository.findByRoleIdAndIsDeletedFalse(testRole.getRoleId()))
                .thenReturn(Optional.of(testRole));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> roleService.updateRole(testRole.getRoleId(), request, actorId, clientIp, userAgent));
    }

    @Test
    void updateRole_RoleNameAlreadyExists() {
        // Arrange
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getRoleName()).thenReturn("Existing Role Name");

        when(roleRepository.findByRoleIdAndIsDeletedFalse(testRole.getRoleId()))
                .thenReturn(Optional.of(testRole));
        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> roleService.updateRole(testRole.getRoleId(), request, actorId, clientIp, userAgent));
    }

    @Test
    void updateRole_DuplicatePrivilegeSet() {
        // Arrange
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getRoleName()).thenReturn("Updated Role");
        when(request.getPrivilegeCodes()).thenReturn(List.of("READ_ONLY"));

        Role existingRole = Role.builder()
                .roleId(UUID.randomUUID().toString())
                .roleCode("ROLE_OTHER")
                .roleName("Other Role")
                .rolePrivileges(new LinkedHashSet<>())
                .build();

        RolePrivilege rp = RolePrivilege.builder()
                .role(existingRole)
                .privilege(testPrivilege)
                .build();
        existingRole.getRolePrivileges().add(rp);

        when(roleRepository.findByRoleIdAndIsDeletedFalse(testRole.getRoleId()))
                .thenReturn(Optional.of(testRole));
        when(roleRepository.existsByRoleNameAndIsDeletedFalse(anyString())).thenReturn(false);
        when(privilegeRepository.findByPrivilegeCodeAndIsDeletedFalse(any(PrivilegeCode.class)))
                .thenReturn(Optional.of(testPrivilege));
        when(roleRepository.findAll()).thenReturn(List.of(existingRole));

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> roleService.updateRole(testRole.getRoleId(), request, actorId, clientIp, userAgent));
    }

    @Test
    void deleteRole_Success() {
        // Arrange
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(actorId);

            DeleteRoleResponse expectedResponse = DeleteRoleResponse.builder()
                    .roleId(testRole.getRoleId())
                    .deleted(true)
                    .hard(false)
                    .deletedAt(LocalDateTime.now())
                    .deletedBy(actorId)
                    .build();

            when(roleRepository.findById(testRole.getRoleId())).thenReturn(Optional.of(testRole));
            when(userRepository.existsByRole_RoleId(testRole.getRoleId())).thenReturn(false);
            when(roleRepository.save(any(Role.class))).thenReturn(testRole);

            // Act
            DeleteRoleResponse result = roleService.deleteRole(testRole.getRoleId());

            // Assert
            assertNotNull(result);
            assertEquals(testRole.getRoleId(), result.getRoleId());
            assertTrue(result.isDeleted());
            assertFalse(result.isHard());
            verify(roleRepository).save(any(Role.class));
            verify(auditLogRepository).save(any(AuditLog.class));
        }
    }

    @Test
    void deleteRole_RoleNotFound() {
        // Arrange
        when(roleRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> roleService.deleteRole("non-existent"));
    }

    @Test
    void deleteRole_SystemRole() {
        // Arrange
        testRole.setSystem(true);
        when(roleRepository.findById(testRole.getRoleId())).thenReturn(Optional.of(testRole));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> roleService.deleteRole(testRole.getRoleId()));
    }

    @Test
    void deleteRole_RoleInUse() {
        // Arrange
        when(roleRepository.findById(testRole.getRoleId())).thenReturn(Optional.of(testRole));
        when(userRepository.existsByRole_RoleId(testRole.getRoleId())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> roleService.deleteRole(testRole.getRoleId()));
    }

    @Test
    void list_Success() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                null, null, null, null, null,
                0, 20, "roleName", "asc"
        );

        Page<Role> page = new PageImpl<>(List.of(testRole));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(roleRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_WithFilters() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                "test", "ROLE_TEST", true,
                LocalDateTime.now().minusDays(7), LocalDateTime.now(),
                0, 20, "roleCode", "desc"
        );

        Page<Role> page = new PageImpl<>(List.of(testRole));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        verify(roleRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_EmptyResult() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                "nonexistent", null, null, null, null,
                0, 20, "roleName", "asc"
        );

        Page<Role> emptyPage = new PageImpl<>(Collections.emptyList());
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
    }

    @Test
    void list_DefaultPagination() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                null, null, null, null, null,
                null, null, null, null // All defaults
        );

        Page<Role> page = new PageImpl<>(List.of(testRole));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        assertEquals(20, result.size()); // Default size
        assertEquals(0, result.page()); // Default page
    }

    @Test
    void list_InvalidSortBy() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                null, null, null, null, null,
                0, 20, "invalidField", "asc"
        );

        Page<Role> page = new PageImpl<>(List.of(testRole));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        // Should default to "roleName" when invalid sortBy is provided
        verify(roleRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void list_MaxPageSize() {
        // Arrange
        RoleListQuery query = new RoleListQuery(
                null, null, null, null, null,
                0, 200, "roleName", "asc" // Size > 100 should be capped at 100
        );

        Page<Role> page = new PageImpl<>(List.of(testRole));
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PageResult<RoleListItem> result = roleService.list(query);

        // Assert
        assertNotNull(result);
        verify(roleRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}