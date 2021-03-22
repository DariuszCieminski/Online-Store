package pl.swaggerexample.security.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Profile("jwt")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "Bearer ";

    private final JwtManager jwt;

    @Autowired
    public JwtAuthorizationFilter(JwtManager jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null) {
            String token = header.substring(AUTH_PREFIX.length());
            if (jwt.isTokenValid(token)) {
                SecurityContextHolder.getContext().setAuthentication(jwt.getAuthentication(token));
            }
        }

        filterChain.doFilter(request, response);
    }
}