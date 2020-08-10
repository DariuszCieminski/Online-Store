package pl.swaggerexample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.security.JwtAuthenticationEntryPoint;
import pl.swaggerexample.security.JwtAuthenticationFilter;
import pl.swaggerexample.security.JwtAuthorizationFilter;
import pl.swaggerexample.security.JwtManager;
import pl.swaggerexample.service.AuthenticationService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
	private final AuthenticationService authenticationService;
	private final JwtManager jwtManager;
	
	@Autowired
	public SecurityConfig(AuthenticationService authenticationService, JwtManager jwtManager)
	{
		this.authenticationService = authenticationService;
		this.jwtManager = jwtManager;
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.inMemoryAuthentication().withUser(User.builder().username("test@test.com").password("{noop}test").roles(Role.USER.name(), Role.MANAGER.name(), Role.DEVELOPER.name()).build());
		auth.userDetailsService(authenticationService).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.addFilter(new JwtAuthenticationFilter(this.authenticationManagerBean(), jwtManager));
		http.addFilterBefore(new JwtAuthorizationFilter(jwtManager), JwtAuthenticationFilter.class);
		http.authorizeRequests().antMatchers(HttpMethod.DELETE).hasRole(Role.MANAGER.name()).antMatchers("/v2/api-docs", "/swagger-ui.html").hasRole(Role.DEVELOPER.name()).anyRequest().authenticated().and().csrf().disable().httpBasic().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint());
	}
}