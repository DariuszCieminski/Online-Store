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
		User user = userService.getUserByEmail(username);
		
		if (user == null) throw new UsernameNotFoundException("There is no user with e-mail address: " + username);
		
		Set<SimpleGrantedAuthority> roles = new HashSet<>();
		for (Role role : user.getRoles())
		{
			roles.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
		}
		
		return org.springframework.security.core.userdetails.User.builder().username(user.getEmail()).password(user.getPassword()).authorities(roles).build();
	}
}