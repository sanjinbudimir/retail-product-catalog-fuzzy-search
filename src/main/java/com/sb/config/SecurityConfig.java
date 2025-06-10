package com.sb.config;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecret;

    @Value("${custom.client.id}")
    private String clientId;

    @Value("${custom.client.secret}")
    private String clientSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/generate-token", "/api/health").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder())))
            .addFilterBefore(new ClientCredentialsFilter(clientId, clientSecret), BasicAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());  // Disable CSRF for testing
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256")).build();
    }

    static class ClientCredentialsFilter extends BasicAuthenticationFilter {
        private static final Logger logger = LoggerFactory.getLogger(ClientCredentialsFilter.class);
        private final String clientId;
        private final String clientSecret;

        public ClientCredentialsFilter(String clientId, String clientSecret) {
            super(authentication -> authentication);
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            // Skip processing if header starts with Bearer — let Resource Server handle it
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            if (authHeader == null || authHeader.isBlank()) {
                logger.warn("Authorization header missing");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
                return;
            }
            if (!authHeader.startsWith("Basic ")) {
                logger.warn("Authorization header does not start with Basic: {}", authHeader);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization header");
                return;
            }

            try {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                String[] values = credentials.split(":", 2);
                if (values.length != 2) {
                    logger.warn("Invalid Basic auth credentials format");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                    return;
                }
                String id = values[0];
                String secret = values[1];
                if (!Objects.equals(id, clientId) || !Objects.equals(secret, clientSecret)) {
                    logger.warn("Client ID or secret mismatch");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                    return;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Error decoding credentials: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid client credentials");
                return;
            }

            chain.doFilter(request, response);
        }

    }
}
