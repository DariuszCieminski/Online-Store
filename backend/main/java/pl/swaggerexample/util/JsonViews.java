package pl.swaggerexample.util;

public class JsonViews {

    public interface UserAuthentication {}

    public interface UserDetailed extends UserAuthentication {}

    public interface OrderSimple {}

    public interface OrderDetailed extends OrderSimple {}
}