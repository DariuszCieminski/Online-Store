package pl.swaggerexample.security.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.swaggerexample.model.enums.Role;

@Component
@Profile("jwt")
public class SwaggerAuthenticationFilter extends OncePerRequestFilter {

    public static final List<String> SWAGGER_PATH_MATCHERS =
        Collections.unmodifiableList(Arrays.asList("/swagger-ui/**", "/v2/api-docs", "/swagger-resources/**"));
    private final JwtManager jwt;
    private final AntPathMatcher pathMatcher;

    @Autowired
    public SwaggerAuthenticationFilter(JwtManager jwt) {
        this.jwt = jwt;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (SWAGGER_PATH_MATCHERS.stream().anyMatch(swaggerUri -> pathMatcher.match(swaggerUri, request.getRequestURI()))) {
            SecurityContextHolder.clearContext();
            if (request.getCookies() != null) {
                Optional<Cookie> swaggerCookie = Arrays.stream(request.getCookies())
                                                       .filter(cookie -> cookie.getName().equals("swagger_id")).findFirst();

                if (swaggerCookie.isPresent() && jwt.isSwaggerCookie(swaggerCookie.get().getValue())) {
                    Authentication authentication =
                        new UsernamePasswordAuthenticationToken(jwt.getUsername(swaggerCookie.get().getValue()), "",
                                                                Collections.singleton(new SimpleGrantedAuthority(
                                                                    "ROLE_" + Role.DEVELOPER.name())));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}