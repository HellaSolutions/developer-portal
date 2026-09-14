package com.media.portal.developerportal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class GatewayKeyFilter extends OncePerRequestFilter {

    private final byte[] expectedToken;

    public GatewayKeyFilter(String expectedToken) {
        this.expectedToken = expectedToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String tokenHeader = request.getHeader("X-Gateway-Key");
        if (tokenHeader != null && MessageDigest.isEqual(tokenHeader.getBytes(StandardCharsets.UTF_8), expectedToken)) {
            // Token is valid; create an authenticated user context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "gateway",
                    null,
                    AuthorityUtils.createAuthorityList("GATEWAY")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
