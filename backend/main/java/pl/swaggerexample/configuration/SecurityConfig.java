package pl.swaggerexample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.security.*;
import pl.swaggerexample.service.AuthenticationService;

import java.util.Arrays;
import java.util.Collections;

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
	protected void configure(AuthenticationManagerBuilder auth)
	{
		auth.authenticationProvider(new CustomAuthenticationProvider(authenticationService, new BCryptPasswordEncoder()));
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
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
		http.authorizeRequests().antMatchers(HttpMethod.DELETE).hasRole(Role.MANAGER.name()).antMatchers(HttpMethod.GET, "/api/users/currentuser").authenticated().antMatchers(HttpMethod.GET, "/api/users/**", "/api/orders/**").hasRole(Role.MANAGER.name()).antMatchers(HttpMethod.POST, "/api/products").hasRole(Role.MANAGER.name()).antMatchers(HttpMethod.PUT, "/api/products").hasRole(Role.MANAGER.name()).antMatchers(HttpMethod.POST, "/api/users").permitAll().anyRequest().authenticated().and().csrf().disable().logout().deleteCookies("swagger_id").logoutSuccessHandler(new CustomLogoutSuccessHandler()).and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint());
	}
}