package gtemp.gtemp_io.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long getCurrentUserId() {
        System.out.println("=== SecurityUtil.getCurrentUserId() ===");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            System.out.println("Authentication is NULL - JWT filter didn't set auth");
            throw new RuntimeException("Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        System.out.println("Principal: " + principal);
        System.out.println("Principal class: " + principal.getClass().getName());

        if (principal instanceof String) {
            System.out.println("Principal is String: '" + principal + "'");
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid user ID format: " + principal);
            }
        } else if (principal instanceof Long) {
            System.out.println("Principal is Long: " + principal);
            return (Long) principal;
        } else {
            System.out.println("Unexpected principal type: " + principal.getClass());
            throw new RuntimeException("Invalid user ID format: " + principal);
        }
    }
}