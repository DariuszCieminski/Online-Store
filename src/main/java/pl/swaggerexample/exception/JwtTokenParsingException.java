package pl.swaggerexample.exception;

public class JwtTokenParsingException extends InvalidAuthenticationAttemptException {

    public JwtTokenParsingException(String msg) {
        super(msg);
    }
}