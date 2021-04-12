package pl.onlinestore.util;

public class JsonViews {

    public interface UserSimple {}

    public interface UserDetailed extends UserSimple {}

    public interface OrderSimple {}

    public interface OrderDetailed extends OrderSimple {}
}