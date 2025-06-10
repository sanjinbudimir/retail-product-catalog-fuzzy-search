package com.sb.api;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class JwtTokenApi {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenApi.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecret;

    /**
     * Generates a signed JWT using the configured secret key.
     * Parameters:
     *  - subject: user identifier (e.g. "testuser")
     *  - scope: scope or roles (default: "read")
     *  - ttlSeconds: token time to live in seconds (default: 3600)
     *
     * Returns: a signed JWT as a string.
     */
    @PostMapping("/generate-token")
    public String generateToken(@RequestParam String subject,
        @RequestParam(defaultValue = "read") String scope,
        @RequestParam(defaultValue = "3600") long ttlSeconds) throws Exception {

        if (subject == null || subject.trim().isEmpty()) {
            logger.warn("Subject is null or empty");
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        if (ttlSeconds <= 0) {
            logger.warn("TTL seconds is invalid: {}", ttlSeconds);
            throw new IllegalArgumentException("TTL seconds must be positive");
        }

        logger.info("POST /api/generate-token - Generating token for subject: {}, scope: {}, ttlSeconds: {}",
            subject, scope, ttlSeconds);
        logger.debug("Using JWT Secret: {}", jwtSecret);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
        .subject(subject)
        .claim("scope", scope)
        .issuer("local-auth-server")
        .expirationTime(new Date(System.currentTimeMillis() + ttlSeconds * 1000))
        .build();

        JWSSigner signer = new MACSigner(jwtSecret.getBytes());
        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.HS256),
            claimsSet
        );

        signedJWT.sign(signer);

        String token = signedJWT.serialize();
        logger.info("Generated JWT token for subject {}: {}", subject, token);

        return signedJWT.serialize();
    }
}
