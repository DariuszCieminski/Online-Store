package pl.swaggerexample.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtTokenParsingException extends AuthenticationException
{
	public JwtTokenParsingException(String msg)
	{
		super(msg);
	}
}