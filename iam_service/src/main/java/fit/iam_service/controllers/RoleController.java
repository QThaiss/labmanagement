package fit.iam_service.controllers;

import fit.iam_service.dtos.PageResult;
import fit.iam_service.dtos.request.CreateRoleRequest;
import fit.iam_service.dtos.request.RoleListQuery;
import fit.iam_service.dtos.request.UpdateRoleRequest;
import fit.iam_service.dtos.response.*;
import fit.iam_service.security.UserDetailsImpl;
import fit.iam_service.services.RoleService;
import fit.iam_service.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<CreateRoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest req,
            HttpServletRequest httpServletRequest
    ) {
        // Harcode để test
        String actorId = SecurityUtils.getCurrentUserId();
        String clientIp = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");

        CreateRoleResponse response = roleService.createRole(req, actorId, clientIp, userAgent);


        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Role created successfully", response, httpServletRequest.getRequestURI())
        );
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') or #id == principal.id")
    public ResponseEntity<UpdateRoleResponse> updateRole(
            @PathVariable("roleId") String roleId,
            @Valid @RequestBody UpdateRoleRequest request,
            HttpServletRequest httpRequest
    ) {

        String actorId = SecurityUtils.getCurrentUserId();

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        UpdateRoleResponse response = roleService.updateRole(roleId, request, actorId, ip, userAgent);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER') or #id == principal.id")
    public ResponseEntity<?> deleteRole(@PathVariable String roleId) {
        DeleteRoleResponse response = roleService.deleteRole(roleId);
        return ResponseEntity.ok(response); // hoặc ResponseEntity.noContent().build()
    }
    @GetMapping
    public ResponseEntity<PageResult<RoleListItem>> listRoles(RoleListQuery query) {
        return ResponseEntity.ok(roleService.list(query));
    }

}
