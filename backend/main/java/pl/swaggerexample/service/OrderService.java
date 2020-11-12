package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.OrderDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Order;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService implements EntityService<Order>
{
	private final OrderDao orderDao;
	
	@Autowired
	public OrderService(OrderDao orderDao)
	{
		this.orderDao = orderDao;
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