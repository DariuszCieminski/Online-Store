package pl.swaggerexample.configuration;

import java.util.Arrays;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.security.CustomLogoutSuccessHandler;
import pl.swaggerexample.security.JwtAuthenticationEntryPoint;
import pl.swaggerexample.security.JwtAuthenticationFilter;
import pl.swaggerexample.security.JwtAuthorizationFilter;
import pl.swaggerexample.security.JwtManager;
import pl.swaggerexample.security.SwaggerAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final JwtManager jwtManager;

    @Autowired
    public SecurityConfig(AuthenticationProvider authenticationProvider, JwtManager jwtManager) {
        this.authenticationProvider = authenticationProvider;
        this.jwtManager = jwtManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilter(new JwtAuthenticationFilter(this.authenticationManagerBean(), jwtManager));
        http.addFilterBefore(new SwaggerAuthenticationFilter(jwtManager), JwtAuthenticationFilter.class);
        http.addFilterBefore(new JwtAuthorizationFilter(jwtManager), SwaggerAuthenticationFilter.class);

        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setMaxAge(1800L);
        corsConfig.setAllowCredentials(true);

        http.cors().configurationSource(source -> corsConfig);
        http.authorizeRequests()
                .antMatchers(HttpMethod.DELETE).hasRole(Role.MANAGER.name())
                .antMatchers(HttpMethod.GET, "/api/orders/buyer").authenticated()
                .antMatchers(HttpMethod.GET, "/api/users/**", "/api/orders/**").hasRole(Role.MANAGER.name())
                .antMatchers(HttpMethod.POST, "/api/products").hasRole(Role.MANAGER.name())
                .antMatchers(HttpMethod.PUT, "/api/products", "/api/users").hasRole(Role.MANAGER.name())
                .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                .anyRequest().authenticated().and()
            .csrf().disable()
            .logout().deleteCookies("swagger_id").logoutSuccessHandler(new CustomLogoutSuccessHandler()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint());
    }
}