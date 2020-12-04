package pl.swaggerexample.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashSet;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.swaggerexample.exception.JwtTokenParsingException;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.util.JsonViews;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager manager;
    private final JwtManager jwt;
    private final ObjectMapper mapper;

    public JwtAuthenticationFilter(AuthenticationManager manager, JwtManager jwt) {
        this.manager = manager;
        this.jwt = jwt;
        this.mapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        try {
            JsonNode requestBody = mapper.readTree(request.getReader());

            if (isReAuthentication(requestBody)) {
                if (request.getHeader("Authorization") == null) {
                    throw new AuthenticationException("Missing 'Authorization' header.") {};
                }

                return reauthenticate(requestBody);
            }

            User user = mapper.treeToValue(requestBody, User.class);
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword(), new HashSet<>());

            return manager.authenticate(authenticationToken);
        } catch (IOException e) {
            throw new JwtTokenParsingException(e.getLocalizedMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {
        if (authResult.getPrincipal() instanceof AuthenticatedUser) {
            User user = ((AuthenticatedUser) authResult.getPrincipal()).getUser();
            ((UsernamePasswordAuthenticationToken) authResult).setDetails(user.getId());
            setSwaggerCookie(response, authResult);
            response.getWriter().print(writeAuthentication(authResult));
        } else {
            response.getWriter().print(writeReauthentication(authResult));
        }

        response.flushBuffer();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(failed.getMessage());
        response.flushBuffer();
    }

    private Authentication reauthenticate(JsonNode requestBody) {
        String accessToken = requestBody.get("access_token").textValue();
        String refreshToken = requestBody.get("refresh_token").textValue();

        if (!jwt.isTokenValid(refreshToken)) {
            throw new JwtTokenParsingException("Refresh token is not valid.");
        }

        if (!jwt.getUsername(accessToken).equals(jwt.getUsername(refreshToken))) {
            throw new JwtTokenParsingException("Tokens are not matched.");
        }

        UsernamePasswordAuthenticationToken authentication = jwt.getAuthentication(accessToken);
        authentication.setDetails(jwt.getUserId(accessToken));

        return authentication;
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

        return mapper.writerWithView(JsonViews.UserAuthentication.class).writeValueAsString(node);
    }
}