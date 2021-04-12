package pl.onlinestore.security;

import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.onlinestore.model.User;
import pl.onlinestore.model.enums.Role;
import pl.onlinestore.service.AuthenticationService;

@Component
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;

    private static final AuthenticatedUser ADMIN =
        new AuthenticatedUser(new User("Administrator",
                                       "",
                                       "admin@test.com",
                                       "{bcrypt}$2a$10$OSu2rKAQ7sIwJ.lKC7h15ec9tAnGepg0TsYt37E5x2extAuK1TTVC",
                                       new HashSet<>(Arrays.asList(Role.USER, Role.MANAGER, Role.DEVELOPER))));

    @Autowired
    public CustomAuthenticationProvider(AuthenticationService authenticationService, PasswordEncoder passwordEncoder) {
        this.authenticationService = authenticationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        if (!userDetails.equals(ADMIN) && !passwordEncoder.matches((CharSequence) authentication.getCredentials(),
                                                                   userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        if (isAdmin(username, authentication)) {
            return ADMIN;
        } else {
            return authenticationService.loadUserByUsername(username);
        }
    }

    private boolean isAdmin(String username, UsernamePasswordAuthenticationToken authentication) {
        return ADMIN.getUsername().equals(username) &&
               passwordEncoder.matches((CharSequence) authentication.getCredentials(), ADMIN.getPassword());
    }
}