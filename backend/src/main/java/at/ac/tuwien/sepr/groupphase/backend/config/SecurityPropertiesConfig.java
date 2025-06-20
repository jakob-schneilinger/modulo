package at.ac.tuwien.sepr.groupphase.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityPropertiesConfig {

    @Bean
    @ConfigurationProperties(prefix = "security.auth")
    protected Auth auth() {
        return new Auth();
    }

    @Bean
    @ConfigurationProperties(prefix = "security.jwt")
    protected Jwt jwt() {
        return new Jwt();
    }

    @Setter
    @Getter
    public static class Auth {
        private String header;
        private String prefix;
        private String loginUri;

    }

    @Setter
    @Getter
    public static class Jwt {
        private String secret;
        private String type;
        private String issuer;
        private String audience;
        private Long expirationTime;

    }
}
