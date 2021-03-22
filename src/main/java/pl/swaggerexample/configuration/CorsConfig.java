package pl.swaggerexample.configuration;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.web.cors.CorsConfiguration;

public class CorsConfig implements Customizer<CorsConfigurer<HttpSecurity>> {

    @Override
    public void customize(CorsConfigurer<HttpSecurity> corsConfigurer) {
        corsConfigurer.configurationSource(source -> {
            CorsConfiguration corsConfig = new CorsConfiguration();
            corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
            corsConfig.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));
            corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN"));
            corsConfig.setMaxAge(1800L);
            corsConfig.setAllowCredentials(true);

            return corsConfig;
        });
    }
}