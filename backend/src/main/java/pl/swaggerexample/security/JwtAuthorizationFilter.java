package pl.swaggerexample.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends OncePerRequestFilter
{
	private static final String AUTH_HEADER = "Authorization";
	private static final String AUTH_PREFIX = "Bearer ";
	private final JwtManager jwt;
	
	public JwtAuthorizationFilter(JwtManager jwt)
	{
		this.jwt = jwt;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
	{
		String header = request.getHeader(AUTH_HEADER);
		if (header != null)
		{
			String token = header.substring(AUTH_PREFIX.length());
			if (jwt.isTokenValid(token))
			{
				UsernamePasswordAuthenticationToken user = jwt.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(user);
			}
		}
		
		filterChain.doFilter(request, response);
	}
}