/*
 * @ {#} CustomAuthenticationProvider.java   1.0     09/10/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.iam_service.security;

import fit.iam_service.utils.RsaDecryptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * @description: Custom authentication provider that uses RSA decryption for password verification
 * @author: Tran Hien Vinh
 * @date:   09/10/2025
 * @version:    1.0
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final RsaDecryptUtils rsaDecryptUtils;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String encryptedPassword = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String rawPassword = rsaDecryptUtils.decrypt(encryptedPassword);

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BadCredentialsException("Password decryption failed (empty result)");
        }

        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
