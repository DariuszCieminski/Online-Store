package pl.swaggerexample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.service.AuthenticationService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
	private final AuthenticationService authenticationService;
	
	@Autowired
	public SecurityConfig(AuthenticationService authenticationService) {this.authenticationService = authenticationService;}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception
	{
		auth.inMemoryAuthentication().withUser(User.builder().username("test@test.com").password("{noop}test").roles(Role.USER.name(), Role.MANAGER.name(), Role.DEVELOPER.name()).build());
		auth.userDetailsService(authenticationService).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests().antMatchers(HttpMethod.DELETE).hasRole(Role.MANAGER.name()).antMatchers("/v2/api-docs", "/swagger-ui.html").hasRole(Role.DEVELOPER.name()).anyRequest().authenticated().and().csrf().disable().httpBasic().and().sessionManagement().maximumSessions(1).maxSessionsPreventsLogin(true);
	}
}