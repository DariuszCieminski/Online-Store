package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.OrderDao;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService implements EntityService<Order>
{
	private final OrderDao orderDao;
	private final ProductDao productDao;
	
	@Autowired
	public OrderService(OrderDao orderDao, ProductDao productDao)
	{
		this.orderDao = orderDao;
		this.productDao = productDao;
	}
	
	@Override
	public Order getById(Long id)
	{
		return orderDao.findById(id).orElseThrow(() -> new NotFoundException("There is no order with id: " + id));
	}
	
	@Override
	public List<Order> getAll()
	{
		List<Order> orders = new ArrayList<>();
		orderDao.findAll().forEach(orders::add);
		return orders;
	}
	
	@Override
	public Order add(Order object)
	{
		Map<Long, Integer> orderedProductsQuantities = object.getItems().stream().collect(Collectors.toMap(orderItem -> orderItem.getProduct().getId(), OrderItem::getQuantity));
		Iterable<Product> orderedProducts = productDao.findAllById(orderedProductsQuantities.keySet());
		
		orderedProducts.forEach(product -> product.setQuantity(product.getQuantity() - orderedProductsQuantities.get(product.getId())));
		productDao.saveAll(orderedProducts);
		
		return orderDao.save(object);
	}
	
	@Override
	public Order update(Order object)
	{
		throw new UnsupportedOperationException("Order update is unsupported.");
	}
	
	@Override
	public void delete(Long id)
	{
		Order order = getById(id);
		orderDao.delete(order);
	}
}