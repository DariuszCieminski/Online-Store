package pl.swaggerexample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import pl.swaggerexample.security.CustomAuthenticationEntryPoint;
import pl.swaggerexample.security.CustomLogoutSuccessHandler;
import pl.swaggerexample.security.SwaggerAuthenticationFilter;
import pl.swaggerexample.security.jwt.JwtAuthenticationFilter;
import pl.swaggerexample.security.jwt.JwtAuthorizationFilter;
import pl.swaggerexample.security.jwt.JwtManager;

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

        http.authorizeRequests(new RequestAuthorizationConfigurer())
            .cors(new CorsConfig())
            .csrf().disable()
            .logout().deleteCookies("swagger_id").logoutSuccessHandler(new CustomLogoutSuccessHandler()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }
}