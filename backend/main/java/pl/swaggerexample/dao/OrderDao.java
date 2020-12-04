package pl.swaggerexample.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.Order;

public interface OrderDao extends CrudRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"buyer", "buyer.roles", "items", "items.product"})
    Optional<Order> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = {"buyer", "buyer.roles", "items", "items.product"})
    Iterable<Order> findAll();

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.images"})
    List<Order> getOrdersByBuyerId(Long buyerId);
}