package pl.swaggerexample.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.service.AuthenticationService;

import java.util.Arrays;
import java.util.HashSet;

public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider
{
	private final AuthenticationService service;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticatedUser testUser = new AuthenticatedUser(new User("Użytkownik", "testowy", "test@test.com", "test", new HashSet<>(Arrays.asList(Role.USER, Role.MANAGER, Role.DEVELOPER))));
	
	public CustomAuthenticationProvider(AuthenticationService service, PasswordEncoder passwordEncoder)
	{
		this.service = service;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
		if (!userDetails.equals(testUser))
		{
			if (!passwordEncoder.matches((CharSequence) authentication.getCredentials(), userDetails.getPassword()))
				throw new BadCredentialsException("Invalid password");
		}
	}
	
	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
	{
		if (testUser.getUsername().equals(authentication.getName()) && testUser.getPassword().equals(authentication.getCredentials()))
		{
			return testUser;
		}
		
		else return service.loadUserByUsername(username);
	}
}