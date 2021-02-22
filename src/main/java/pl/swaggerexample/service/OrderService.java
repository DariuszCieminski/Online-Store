package pl.swaggerexample.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.OrderDao;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.OrderStatus;

@Service
public class OrderService implements EntityService<Order> {

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final UserDao userDao;

    @Autowired
    public OrderService(OrderDao orderDao, ProductDao productDao, UserDao userDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.userDao = userDao;
    }

    @Override
    public Order getById(Long id) {
        return orderDao.findById(id).orElseThrow(() -> new NotFoundException("There is no order with id: " + id));
    }

    public List<Order> getByBuyerId(Long buyerId) {
        if (!userDao.existsById(buyerId)) {
            throw new NotFoundException("There is no buyer with id: " + buyerId);
        }

        return orderDao.getOrdersByBuyerId(buyerId);
    }

    public List<Order> getByCurrentUser() {
        Optional<User> currentUser = userDao.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!currentUser.isPresent()) {
            throw new NotFoundException("Invalid current user authentication.");
        }

        return orderDao.getOrdersByBuyerId(currentUser.get().getId());
    }

    @Override
    public List<Order> getAll() {
        List<Order> orders = new ArrayList<>();
        orderDao.findAll().forEach(orders::add);
        return orders;
    }

    @Override
    public Order add(Order object) {
        Optional<User> currentUser = userDao.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        if (!currentUser.isPresent()) {
            throw new NotFoundException("Invalid current user authentication.");
        }

        Map<Long, Integer> orderedProductsQuantitiesMap = object.getItems().stream().collect(
            Collectors.toMap(orderItem -> orderItem.getProduct().getId(), OrderItem::getQuantity));

        Iterable<Product> orderedProducts = productDao.findAllById(orderedProductsQuantitiesMap.keySet());
        orderedProducts
            .forEach(product -> product.setQuantity(product.getQuantity() - orderedProductsQuantitiesMap.get(product.getId())));
        productDao.saveAll(orderedProducts);

        object.setBuyer(currentUser.get());
        object.setStatus(OrderStatus.CREATED);
        return orderDao.save(object);
    }

    @Override
    public Order update(Order object) {
        throw new UnsupportedOperationException("Order update is unsupported.");
    }

    @Override
    public void delete(Long id) {
        Order order = getById(id);
        orderDao.delete(order);
    }
}