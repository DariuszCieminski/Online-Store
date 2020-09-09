package pl.swaggerexample.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.swaggerexample.exception.JwtTokenParsingException;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.User;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter
{
	private final AuthenticationManager manager;
	private final JwtManager jwt;
	private final ObjectMapper mapper;
	
	public JwtAuthenticationFilter(AuthenticationManager manager, JwtManager jwt)
	{
		this.manager = manager;
		this.jwt = jwt;
		this.mapper = new ObjectMapper();
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException
	{
		try
		{
			JsonNode requestBody = mapper.readTree(request.getReader());
			
			if (isReAuthentication(requestBody))
			{
				String accessToken = requestBody.get("access_token").textValue();
				String refreshToken = requestBody.get("refresh_token").textValue();
				
				if (!jwt.isTokenValid(refreshToken)) throw new JwtTokenParsingException("Refresh token is not valid.");
				if (!jwt.getUsername(accessToken).equals(jwt.getUsername(refreshToken)))
					throw new JwtTokenParsingException("Tokens are not matched.");
				response.addHeader("reauth", "true");
				
				return jwt.getAuthentication(accessToken);
			}
			
			User user = mapper.treeToValue(requestBody, User.class);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword(), new HashSet<>());
			return manager.authenticate(authenticationToken);
		}
		
		catch (IOException e)
		{
			throw new JwtTokenParsingException(e.getLocalizedMessage());
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException
	{
		setSwaggerCookie(response, authResult);
		response.getWriter().print(mapper.writeValueAsString(response.containsHeader("reauth") ? writeReauthentication(authResult) : writeAuthentication(authResult)));
		response.flushBuffer();
	}
	
	private void setSwaggerCookie(HttpServletResponse response, Authentication authentication)
	{
		AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
		if (principal.getUser().getRoles().contains(Role.DEVELOPER))
		{
			Cookie cookie = new Cookie("swagger_id", jwt.generateSwaggerToken(authentication));
			cookie.setPath("/");
			cookie.setHttpOnly(true);
			response.addCookie(cookie);
		}
	}
	
	private boolean isReAuthentication(JsonNode node)
	{
		boolean hasAccessToken = node.has("access_token");
		boolean hasRefreshToken = node.has("refresh_token");
		
		return hasAccessToken && hasRefreshToken;
	}
	
	private JsonNode writeReauthentication(Authentication authentication)
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("access_token", jwt.generateAccessToken(authentication));
		return node;
	}
	
	private JsonNode writeAuthentication(Authentication authentication)
	{
		User user = ((AuthenticatedUser) authentication.getPrincipal()).getUser();
		List<String> rolesList = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		
		ObjectNode node = mapper.createObjectNode();
		ArrayNode rolesArrayNode = mapper.valueToTree(rolesList);
		
		node.putObject("user")
				.put("firstName", user.getName())
				.put("lastName", user.getSurname())
				.put("email", user.getEmail());
		
		node.with("user").set("address", mapper.valueToTree(user.getAddress()));
		node.with("user").putArray("roles").addAll(rolesArrayNode);
		node.put("access_token", jwt.generateAccessToken(authentication));
		node.put("refresh_token", jwt.generateRefreshToken(authentication));
		
		return node;
	}
}