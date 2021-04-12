package pl.onlinestore.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import pl.onlinestore.security.CustomAuthenticationEntryPoint;
import pl.onlinestore.security.CustomLogoutSuccessHandler;
import pl.onlinestore.security.jwt.JwtAuthenticationFilter;
import pl.onlinestore.security.jwt.JwtAuthorizationFilter;
import pl.onlinestore.security.jwt.SwaggerAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("jwt")
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthorizationFilter authorizationFilter;
    private final SwaggerAuthenticationFilter swaggerFilter;
    private JwtAuthenticationFilter authenticationFilter;

    @Autowired
    public JwtSecurityConfig(AuthenticationProvider authenticationProvider,
                             JwtAuthorizationFilter authorizationFilter,
                             SwaggerAuthenticationFilter swaggerFilter) {
        this.authenticationProvider = authenticationProvider;
        this.authorizationFilter = authorizationFilter;
        this.swaggerFilter = swaggerFilter;
    }

    @Autowired
    public void setAuthenticationFilter(JwtAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(new RequestAuthorizationConfigurer())
            .cors().and()
            .csrf().disable()
            .addFilter(authenticationFilter)
            .addFilterBefore(swaggerFilter, authenticationFilter.getClass())
            .addFilterBefore(authorizationFilter, swaggerFilter.getClass())
            .logout().deleteCookies("swagger_id").logoutSuccessHandler(new CustomLogoutSuccessHandler()).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}