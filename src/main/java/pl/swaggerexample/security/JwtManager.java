package pl.swaggerexample.security;

import io.jsonwebtoken.Claims;
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
	private final long VALIDATION_TIME;
	private final SecretKey SECRET_KEY;
	
	@Autowired
	public JwtManager(@Value("${jwt.token.validation-time}") long validationTime)
	{
		VALIDATION_TIME = validationTime;
		SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	}
	
	public String generateToken(Authentication authentication)
	{
		Date expirationTime = Date.from(LocalDateTime.now()
				.plusMinutes(VALIDATION_TIME)
				.atZone(ZoneOffset.systemDefault())
				.toInstant());
		
		return Jwts.builder()
				.setSubject(authentication.getName())
				.claim("roles", authentication.getAuthorities())
				.setExpiration(expirationTime)
				.signWith(SECRET_KEY)
				.compact();
	}
	
	public boolean isTokenValid(String token)
	{
		return Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getExpiration()
				.after(new Date());
	}
	
	public UsernamePasswordAuthenticationToken getAuthentication(String token)
	{
		Claims claims = Jwts.parserBuilder()
				.setSigningKey(SECRET_KEY)
				.build()
				.parseClaimsJws(token)
				.getBody();
		
		ArrayList<LinkedHashMap<String, String>> roles = claims.get("roles", ArrayList.class);
		List<SimpleGrantedAuthority> authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role.get("authority"))).collect(Collectors.toList());
		
		return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
	}
}