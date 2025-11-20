package fit.iam_service.utils;


import fit.iam_service.exceptions.UnauthorizedException;
import fit.iam_service.security.UserDetailsImpl;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return userPrincipal.getId();
    }
}
