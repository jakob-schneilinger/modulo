package at.ac.tuwien.sepr.groupphase.backend.security;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final JwtUtils jwtUtils;
    private final SecurityProperties securityProperties;

    public JwtAuthorizationFilter(JwtUtils jwtUtils, SecurityProperties securityProperties) {
        this.jwtUtils = jwtUtils;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            String token = extractToken(request);
            if (token != null && jwtUtils.validateJwtToken(token)) {
                String email = jwtUtils.getEmailFromJwtToken(token);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")) // Default role
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            LOGGER.debug("Invalid authorization attempt: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid authorization header or token");
            return;
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(securityProperties.getAuthHeader());
        if (StringUtils.hasText(header) && header.startsWith(securityProperties.getAuthTokenPrefix())) {
            return header.substring(securityProperties.getAuthTokenPrefix().length());
        }
        return null;
    }
}
