package pl.swaggerexample.configuration;

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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import pl.swaggerexample.security.CustomAuthenticationEntryPoint;
import pl.swaggerexample.security.CustomLogoutSuccessHandler;
import pl.swaggerexample.security.session.SessionAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Profile("session")
public class SessionSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;
    private SessionAuthenticationFilter authenticationFilter;

    @Autowired
    public SessionSecurityConfig(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Autowired
    public void setAuthenticationFilter(SessionAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(new RequestAuthorizationConfigurer())
            .cors(new CorsConfig())
            .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
            .addFilter(authenticationFilter)
            .sessionManagement().maximumSessions(1).and().sessionFixation().migrateSession().and()
            .logout().deleteCookies("session_id", "XSRF-TOKEN").logoutSuccessHandler(new CustomLogoutSuccessHandler()).and()
            .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}