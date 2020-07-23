package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.User;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
		
		Set<SimpleGrantedAuthority> roles = new HashSet<>();
		for (Role role : user.get().getRoles())
		{
			roles.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
		}
		
		return org.springframework.security.core.userdetails.User.builder().username(user.get().getEmail()).password(user.get().getPassword()).authorities(roles).build();
	}
}