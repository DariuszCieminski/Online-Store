package pl.onlinestore.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidAuthenticationAttemptException extends AuthenticationException {

    public InvalidAuthenticationAttemptException(String msg) {
        super(msg);
    }
}