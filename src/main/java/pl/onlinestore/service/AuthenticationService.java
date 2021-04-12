package pl.onlinestore.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.onlinestore.model.User;
import pl.onlinestore.security.AuthenticatedUser;

@Service
public class AuthenticationService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public AuthenticationService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userService.getUserByEmail(username);
        return new AuthenticatedUser(
            user.orElseThrow(() -> new UsernameNotFoundException("There is no user with e-mail address: " + username)));
    }
}