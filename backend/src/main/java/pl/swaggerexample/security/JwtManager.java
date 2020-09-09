package pl.swaggerexample.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtManager
{
	private final long ACCESS_TOKEN_VALIDATION_TIME;
	private final long REFRESH_TOKEN_VALIDATION_TIME;
	private final SecretKey SECRET_KEY;
	
	@Autowired
	public JwtManager(@Value("${jwt.access-token.validation-time}") long accessValidationTime, @Value("${jwt.refresh-token.validation-time}") long refreshValidationTime)
	{
		ACCESS_TOKEN_VALIDATION_TIME = accessValidationTime;
		REFRESH_TOKEN_VALIDATION_TIME = refreshValidationTime;
		SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	}
	
	public String generateAccessToken(Authentication authentication)
	{
		Date expirationTime = Date.from(LocalDateTime.now()
				.plusMinutes(ACCESS_TOKEN_VALIDATION_TIME)
				.atZone(ZoneOffset.systemDefault())
				.toInstant());
		
		return Jwts.builder()
				.setSubject(authentication.getName())
				.claim("roles", authentication.getAuthorities())
				.setExpiration(expirationTime)
				.signWith(SECRET_KEY)
				.compact();
	}
	
	public String generateRefreshToken(Authentication authentication)
	{
		Date expirationTime = Date.from(LocalDateTime.now()
				.plusMinutes(REFRESH_TOKEN_VALIDATION_TIME)
				.atZone(ZoneOffset.systemDefault())
				.toInstant());
		
		return Jwts.builder()
				.setSubject(authentication.getName())
				.setExpiration(expirationTime)
				.signWith(SECRET_KEY)
				.compact();
	}
	
	public String generateSwaggerToken(Authentication authentication)
	{
		return Jwts.builder()
				.setSubject(authentication.getName())
				.claim("swagger", true)
				.signWith(SECRET_KEY)
				.compact();
	}
	
	public boolean isTokenValid(String token)
	{
		try
		{
			Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
			return true;
		}
		
		catch (ExpiredJwtException e)
		{
			return false;
		}
	}
	
	public boolean isSwaggerCookie(String cookie)
	{
		return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(cookie).getBody().get("swagger") != null;
	}
	
	public String getUsername(String token)
	{
		try
		{
			return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody().getSubject();
		}
		
		catch (ExpiredJwtException e)
		{
			return e.getClaims().getSubject();
		}
	}
	
	public UsernamePasswordAuthenticationToken getAuthentication(String token)
	{
		Claims claims;
		
		try
		{
			claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
		}
		catch (ExpiredJwtException e)
		{
			claims = e.getClaims();
		}
		
		ArrayList<LinkedHashMap<String, String>> roles = claims.get("roles", ArrayList.class);
		List<SimpleGrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role.get("authority"))).collect(Collectors.toList());
		
		return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
	}
}