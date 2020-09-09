package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.swaggerexample.model.User;
import pl.swaggerexample.security.AuthenticatedUser;

import java.util.Optional;

@Service
public class AuthenticationService implements UserDetailsService
{
	private final UserService userService;
	
	@Autowired
	public AuthenticationService(UserService userService) {this.userService = userService;}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	{
		Optional<User> user = userService.getUserByEmail(username);
		
		if (!user.isPresent()) throw new UsernameNotFoundException("There is no user with e-mail address: " + username);
		
		return new AuthenticatedUser(user.get());
	}
}