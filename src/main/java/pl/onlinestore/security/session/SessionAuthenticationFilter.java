package pl.onlinestore.security.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import pl.onlinestore.exception.InvalidAuthenticationAttemptException;
import pl.onlinestore.model.User;
import pl.onlinestore.security.AuthenticatedUser;
import pl.onlinestore.util.JsonViews.UserDetailed;

@Component
@Profile("session")
public class SessionAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;

    @Autowired
    public SessionAuthenticationFilter(ObjectMapper mapper) {
        this.mapper = mapper;

        setAuthenticationSuccessHandler((request, response, authentication) -> {
            User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();
            response.getWriter().print(mapper.writerWithView(UserDetailed.class).writeValueAsString(user));
            response.flushBuffer();
        });

        setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(exception.getLocalizedMessage());
            response.flushBuffer();
        });
    }

    @Autowired
    @Override
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        try {
            JsonNode requestBody = mapper.readTree(request.getReader());

            JsonNode username = requestBody.get("email");
            JsonNode password = requestBody.get("password");

            if (username == null || password == null) {
                throw new IOException();
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(username.asText(), password.asText());

            return getAuthenticationManager().authenticate(authentication);
        } catch (IOException e) {
            throw new InvalidAuthenticationAttemptException("Invalid login data.");
        }
    }
}