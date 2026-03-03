package com.company.shop.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.company.shop.module.user.exception.UserAuthenticationRequiredException;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    private final EmailNormalizer emailNormalizer;

    public SecurityCurrentUserProvider(EmailNormalizer emailNormalizer) {
        this.emailNormalizer = emailNormalizer;
    }

    @Override
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UserAuthenticationRequiredException();
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.isBlank()) {
            throw new UserAuthenticationRequiredException();
        }
        return emailNormalizer.normalize(principalName);
    }
}
