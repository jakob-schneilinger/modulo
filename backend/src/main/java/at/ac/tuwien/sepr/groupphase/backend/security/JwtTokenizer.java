package at.ac.tuwien.sepr.groupphase.backend.security;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenizer {

    private final SecurityProperties securityProperties;

    public JwtTokenizer(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String getAuthToken(String username, String displayName, String email) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        SecretKey key = Keys.hmacShaKeyFor(signingKey);

        String token = Jwts.builder()
                .header().add("typ", securityProperties.getJwtType()).and()
                .issuer(securityProperties.getJwtIssuer())
                .audience().add(securityProperties.getJwtAudience()).and()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
                .claim("email", email)
                .claim("display_name", displayName)
                .claim("token_created", new Date().getTime())
                .signWith(key, Jwts.SIG.HS512)
                .compact();
        return securityProperties.getAuthTokenPrefix() + token;
    }
}
