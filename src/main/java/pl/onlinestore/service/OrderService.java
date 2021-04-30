package pl.onlinestore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.onlinestore.dao.OrderDao;
import pl.onlinestore.dao.ProductDao;
import pl.onlinestore.dao.UserDao;
import pl.onlinestore.exception.NotFoundException;
import pl.onlinestore.model.Order;
import pl.onlinestore.model.OrderItem;
import pl.onlinestore.model.Product;
import pl.onlinestore.model.User;
import pl.onlinestore.model.enums.OrderStatus;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (object.getId() == null || !orderDao.existsById(object.getId())) {
            throw new EntityNotFoundException("There is no order with id: " + object.getId());
        }

        Order order = orderDao.findById(object.getId()).get();
        order.setStatus(object.getStatus());
        return orderDao.save(object);
    }

    @Override
    public void delete(Long id) {
        Order order = getById(id);
        orderDao.delete(order);
    }
}