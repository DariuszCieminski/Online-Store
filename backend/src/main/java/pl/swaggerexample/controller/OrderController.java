package pl.swaggerexample.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.service.OrderService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Api(description = "Endpoints for getting, adding and removing orders that users make.")
public class OrderController
{
	private final OrderService orderService;
	
	@Autowired
	public OrderController(OrderService orderService)
	{
		this.orderService = orderService;
	}
	
	@GetMapping
	@ApiOperation(value = "Returns list of all made orders")
	public List<Order> getOrders()
	{
		return orderService.getAll();
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Returns a single order by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
	public Order getOrder(@PathVariable @ApiParam(value = "Unique ID of existing order", example = "1") Long id)
	{
		return orderService.getById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Adds new order to database")
	@ApiResponses(value = {@ApiResponse(code = 422, message = "Order has invalid data")})
	public Order addOrder(@Valid @RequestBody @ApiParam(value = "Data of the new order") Order order)
	{
		return orderService.add(order);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Removes a single order by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
	public void deleteOrder(@PathVariable @ApiParam(value = "Unique ID of existing order", example = "1") Long id)
	{
		orderService.delete(id);
	}
}