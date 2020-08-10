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
import pl.swaggerexample.model.User;

import javax.servlet.FilterChain;
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
			User user = mapper.readValue(request.getReader(), User.class);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword(), new HashSet<>());
			return manager.authenticate(authenticationToken);
		}
		catch (IOException e)
		{
			throw new AuthenticationException(e.getLocalizedMessage()) {};
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException
	{
		response.getWriter().print(mapper.writeValueAsString(prepareAuthOutput(authResult)));
		response.flushBuffer();
	}
	
	private JsonNode prepareAuthOutput(Authentication authentication)
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("name", authentication.getName());
		List<String> rolesList = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		ArrayNode rolesArrayNode = mapper.valueToTree(rolesList);
		node.putArray("roles").addAll(rolesArrayNode);
		node.put("token", jwt.generateToken(authentication));
		return node;
	}
}