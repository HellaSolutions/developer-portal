package com.media.portal.developerportal.config;

import com.media.portal.developerportal.controllers.ApiController;
import com.media.portal.developerportal.controllers.IntrospectController;
import com.media.portal.developerportal.model.Api;
import com.media.portal.developerportal.model.SubscriptionType;
import com.media.portal.developerportal.services.ApiService;
import com.media.portal.developerportal.services.Introspection;
import com.media.portal.developerportal.services.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real {@link SecurityConfig} filter chains through MockMvc — unlike the
 * controller *WebMvcTest classes, which import SecurityConfig only to satisfy context
 * startup and then disable the filters entirely via addFilters = false.
 */
@WebMvcTest(controllers = {IntrospectController.class, ApiController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.token=test-gateway-secret")
class SecurityConfigTest {

    private static final String INTROSPECT_BODY = """
            {"key": "pk_someKey"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;
    @MockitoBean
    private ApiService apiService;

    @Test
    void introspect_withValidGatewayKey_reachesController() throws Exception {
        when(subscriptionService.introspect(any()))
                .thenReturn(new Introspection(true, 1L, 2L, "/scopus/v1", SubscriptionType.STANDARD));

        mockMvc.perform(post("/v1/introspect")
                        .header("X-Gateway-Key", "test-gateway-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INTROSPECT_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void introspect_withoutGatewayKey_isRejected() throws Exception {
        mockMvc.perform(post("/v1/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INTROSPECT_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void introspect_withWrongGatewayKey_isRejected() throws Exception {
        mockMvc.perform(post("/v1/introspect")
                        .header("X-Gateway-Key", "not-the-right-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INTROSPECT_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/apis"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withGatewayKeyInsteadOfJwt_isUnauthorized() throws Exception {
        // proves the gateway shared-secret only unlocks /v1/introspect/**, not the JWT-guarded chain
        mockMvc.perform(get("/v1/apis").header("X-Gateway-Key", "test-gateway-secret"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoint_withJwtMissingRequiredScope_isForbidden() throws Exception {
        mockMvc.perform(get("/v1/apis")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_portal:read"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_withJwtHavingRequiredScope_isOk() throws Exception {
        when(apiService.listApis(any(), any())).thenReturn(new PageImpl<Api>(List.of()));

        mockMvc.perform(get("/v1/apis")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_portal:admin"))))
                .andExpect(status().isOk());
    }
}
