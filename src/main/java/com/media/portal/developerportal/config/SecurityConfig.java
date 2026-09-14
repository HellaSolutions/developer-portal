package com.media.portal.developerportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.token:my-default-secret-token}")
    private String gatewayKey;

    @Bean
    @Order(1)
    SecurityFilterChain introspectChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/v1/introspect/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
                .addFilterBefore(new GatewayKeyFilter(gatewayKey), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a.anyRequest().hasAuthority("GATEWAY"))
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain healthChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/health/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(3)
    SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        return http                                   // no securityMatcher: everything else
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(a -> a.anyRequest().hasAuthority("SCOPE_portal:admin"))
                .build();
    }
}
