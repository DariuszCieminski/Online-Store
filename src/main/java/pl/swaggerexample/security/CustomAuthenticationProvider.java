package pl.swaggerexample.security;

import java.util.Arrays;
import java.util.HashSet;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.service.AuthenticationService;

public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final AuthenticationService service;
    private final PasswordEncoder passwordEncoder;

    private static final AuthenticatedUser ADMIN =
        new AuthenticatedUser(new User("Administrator",
                                       "",
                                       "admin@test.com",
                                       "$2a$10$OSu2rKAQ7sIwJ.lKC7h15ec9tAnGepg0TsYt37E5x2extAuK1TTVC",
                                       new HashSet<>(Arrays.asList(Role.USER, Role.MANAGER, Role.DEVELOPER))));

    public CustomAuthenticationProvider(AuthenticationService service, PasswordEncoder passwordEncoder) {
        this.service = service;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        if (!userDetails.equals(ADMIN) && !passwordEncoder.matches((CharSequence) authentication.getCredentials(),
                                                                   userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
        throws AuthenticationException {
        if (ADMIN.getUsername().equals(authentication.getName()) && BCrypt.checkpw((String) authentication.getCredentials(),
                                                                                   ADMIN.getPassword())) {
            return ADMIN;
        } else {
            return service.loadUserByUsername(username);
        }
    }
}