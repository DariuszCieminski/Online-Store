package pl.swaggerexample.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.OrderDao;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.enums.OrderStatus;
import pl.swaggerexample.model.enums.Role;

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

    public List<Order> getOrdersByBuyerId(Long buyerId) {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        if (!currentUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + Role.MANAGER.name()))) {
            if (!currentUser.getDetails().equals(buyerId)) {
                throw new AccessDeniedException("Invalid ID.");
            }
        }

        if (!userDao.existsById(buyerId)) {
            throw new NotFoundException("There is no user with id: " + buyerId);
        }
        return orderDao.getOrdersByBuyerId(buyerId);
    }

    @Override
    public List<Order> getAll() {
        List<Order> orders = new ArrayList<>();
        orderDao.findAll().forEach(orders::add);
        return orders;
    }

    @Override
    public Order add(Order object) {
        Map<Long, Integer> orderedProductsQuantities = object.getItems().stream().collect(
            Collectors.toMap(orderItem -> orderItem.getProduct().getId(), OrderItem::getQuantity));
        Iterable<Product> orderedProducts = productDao.findAllById(orderedProductsQuantities.keySet());

        orderedProducts
            .forEach(product -> product.setQuantity(product.getQuantity() - orderedProductsQuantities.get(product.getId())));
        productDao.saveAll(orderedProducts);

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