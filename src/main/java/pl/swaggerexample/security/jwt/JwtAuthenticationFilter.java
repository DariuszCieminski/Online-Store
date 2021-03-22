package pl.swaggerexample.security.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.servlet.http.Cookie;
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
import pl.swaggerexample.exception.InvalidAuthenticationAttemptException;
import pl.swaggerexample.exception.JwtTokenParsingException;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.security.AuthenticatedUser;
import pl.swaggerexample.util.JsonViews.UserSimple;

@Component
@Profile("jwt")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtManager jwt;
    private final ObjectMapper mapper;

    @Autowired
    public JwtAuthenticationFilter(JwtManager jwt, ObjectMapper mapper) {
        this.jwt = jwt;
        this.mapper = mapper;

        setAuthenticationSuccessHandler((request, response, authentication) -> {
            if (authentication.getPrincipal() instanceof AuthenticatedUser) {
                setSwaggerCookie(response, authentication);
                response.getWriter().print(writeAuthentication(authentication));
            } else {
                response.getWriter().print(writeReauthentication(authentication));
            }
            response.flushBuffer();
        });

        setAuthenticationFailureHandler((request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print(exception.getMessage());
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

            if (isReAuthentication(requestBody)) {
                if (request.getHeader("Authorization") == null) {
                    throw new InvalidAuthenticationAttemptException("Missing 'Authorization' header.");
                }
                return doReauthentication(requestBody);
            }

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

    private Authentication doReauthentication(JsonNode requestBody) {
        String accessToken = requestBody.get("access_token").textValue();
        String refreshToken = requestBody.get("refresh_token").textValue();

        if (!jwt.isTokenValid(refreshToken)) {
            throw new JwtTokenParsingException("Refresh token is not valid.");
        }

        if (!jwt.getUsername(accessToken).equals(jwt.getUsername(refreshToken))) {
            throw new JwtTokenParsingException("Tokens are not matched.");
        }

        return jwt.getAuthentication(accessToken);
    }

    private void setSwaggerCookie(HttpServletResponse response, Authentication authentication) {
        User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();

        if (user.getRoles().contains(Role.DEVELOPER)) {
            Cookie cookie = new Cookie("swagger_id", jwt.generateSwaggerToken(authentication));
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
    }

    private boolean isReAuthentication(JsonNode node) {
        boolean hasAccessToken = node.has("access_token");
        boolean hasRefreshToken = node.has("refresh_token");

        return hasAccessToken && hasRefreshToken;
    }

    private String writeReauthentication(Authentication authentication) throws JsonProcessingException {
        ObjectNode node = mapper.createObjectNode();
        node.put("access_token", jwt.generateAccessToken(authentication));

        return mapper.writeValueAsString(node);
    }

    private String writeAuthentication(Authentication authentication) throws JsonProcessingException {
        User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();
        ObjectNode node = mapper.createObjectNode();

        node.putPOJO("user", user);
        node.put("access_token", jwt.generateAccessToken(authentication));
        node.put("refresh_token", jwt.generateRefreshToken(authentication));

        return mapper.writerWithView(UserSimple.class).writeValueAsString(node);
    }
}