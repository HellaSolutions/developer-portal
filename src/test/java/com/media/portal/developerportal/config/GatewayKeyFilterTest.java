package com.media.portal.developerportal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewayKeyFilterTest {

    private static final String EXPECTED_TOKEN = "expected-gateway-token";

    private final GatewayKeyFilter filter = new GatewayKeyFilter(EXPECTED_TOKEN);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_authenticatesAsGateway_whenHeaderMatchesExpectedToken() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("X-Gateway-Key")).thenReturn(EXPECTED_TOKEN);

        filter.doFilterInternal(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("gateway");
        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("GATEWAY");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_leavesContextUnauthenticated_whenHeaderMissing() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("X-Gateway-Key")).thenReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_leavesContextUnauthenticated_whenHeaderDoesNotMatchExpectedToken() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("X-Gateway-Key")).thenReturn("wrong-token");

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_leavesContextUnauthenticated_whenHeaderIsPrefixOfExpectedToken() throws Exception {
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);
        when(request.getHeader("X-Gateway-Key"))
                .thenReturn(EXPECTED_TOKEN.substring(0, EXPECTED_TOKEN.length() - 1));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
