package at.ac.tuwien.sepr.groupphase.backend.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;

    @Autowired
    public WebSocketConfig(JwtUtils jwtUtils) {
        super();
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new AuthHandshakeInterceptor(jwtUtils))
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/board");
        config.setApplicationDestinationPrefixes("/app");
    }

    private static class AuthHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtUtils jwtUtils;

        public AuthHandshakeInterceptor(JwtUtils jwtUtils) {
            super();
            this.jwtUtils = jwtUtils;
        }

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                WebSocketHandler wsHandler, Map<String, Object> attributes) {

            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest req = servletRequest.getServletRequest();
                String token = req.getParameter("token");

                if (token != null && jwtUtils.validateJwtToken(token)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                WebSocketHandler wsHandler, Exception ex) {
        }
    }
}
