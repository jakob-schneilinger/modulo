package at.ac.tuwien.sepr.groupphase.backend.security;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final JwtTokenizer jwtTokenizer;
    private final SecurityProperties securityProperties;

    public JwtUtils(JwtTokenizer jwtTokenizer, SecurityProperties securityProperties) {
        this.jwtTokenizer = jwtTokenizer;
        this.securityProperties = securityProperties;
    }

    public String generateJwtToken(Authentication authentication) {
        AuthUserDetails userPrincipal = (AuthUserDetails) authentication.getPrincipal();
        return jwtTokenizer.getAuthToken(
                userPrincipal.getUsername(),
                userPrincipal.getUser().getDisplayName(),
                userPrincipal.getUser().getEmail());
    }

    public String getEmailFromJwtToken(String token) {
        try {
            byte[] signingKey = securityProperties.getJwtSecret().getBytes();

            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(signingKey))
                    .build()
                    .parseSignedClaims(token.replace(securityProperties.getAuthTokenPrefix(), ""))
                    .getPayload();

            return claims.get("email", String.class);
        } catch (Exception e) {
            logger.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            byte[] signingKey = securityProperties.getJwtSecret().getBytes();

            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(signingKey))
                    .build()
                    .parseSignedClaims(authToken.replace(securityProperties.getAuthTokenPrefix(), ""));

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromJwtToken(String token) {
        try {
            byte[] signingKey = securityProperties.getJwtSecret().getBytes();

            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey))
                .build()
                .parseSignedClaims(token.replace(securityProperties.getAuthTokenPrefix(), ""))
                .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
}