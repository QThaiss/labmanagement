/*
 * @ {#} UserDetailsImpl.java   1.0     01/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fit.iam_service.entities.RolePrivilege;
import fit.iam_service.entities.User;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description: This class implements UserDetails interface to provide user details for Spring Security
 * @author: Tran Hien Vinh
 * @date:   01/10/2025
 * @version:    1.0
 */
@Builder
@Getter
public class UserDetailsImpl implements UserDetails {
    private String id;

    private String username;

    @JsonIgnore
    private String password;

    private Boolean isDeleted;

    private Collection<? extends GrantedAuthority> authorities;

    private String role;

    private List<String> privileges;

    public UserDetailsImpl(String id,
                           String username,
                           String password,
                           Boolean isDeleted,
                           Collection<? extends GrantedAuthority> authorities,
                           String role,
                           List<String> privileges) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isDeleted = isDeleted;
        this.authorities = authorities;
        this.role = role;
        this.privileges = privileges;
    }

    public static UserDetailsImpl build(User user) {
        // Generate authorities from user roles

        Set<GrantedAuthority> auths = new LinkedHashSet<>();

        // Role
        String roleCode = user.getRole().getRoleCode();
        auths.add(new SimpleGrantedAuthority(roleCode));

        // Privileges
        List<String> privileges = user.getRole().getRolePrivileges().stream()
                .map(rp -> rp.getPrivilege().getPrivilegeCode().name()) // VD: CREATE_PATIENT
                .collect(Collectors.toList());

        privileges.forEach(p -> auths.add(new SimpleGrantedAuthority(p)));

        return UserDetailsImpl.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .isDeleted(user.isDeleted())
                .authorities(auths)
                .role(roleCode)
                .privileges(privileges)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isDeleted == null || !isDeleted;
    }
}
