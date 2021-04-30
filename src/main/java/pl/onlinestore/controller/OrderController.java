package pl.onlinestore.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.onlinestore.model.Order;
import pl.onlinestore.service.OrderService;
import pl.onlinestore.service.ProductService;
import pl.onlinestore.util.JsonViews.OrderDetailed;
import pl.onlinestore.util.JsonViews.OrderSimple;
import pl.onlinestore.validation.OrderValidator;

@RestController
@RequestMapping("/api/orders")
@Api(tags = "Order controller", description = "Endpoints for getting, adding and removing orders that users make.")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    @ApiOperation(value = "Returns list of all orders in the store")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get orders")})
    @JsonView(OrderDetailed.class)
    public List<Order> getOrders() {
        return orderService.getAll();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Returns a single order by its ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get an order"),
                           @ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
    @JsonView(OrderDetailed.class)
    public Order getOrderById(@PathVariable @ApiParam(value = "Unique ID of existing order", example = "1") Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/buyer/{id}")
    @ApiOperation(value = "Returns list of orders made by particular user.")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get orders of specified user"),
                           @ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
    @JsonView(OrderSimple.class)
    public List<Order> getOrdersByBuyerId(
        @PathVariable @ApiParam(value = "Unique ID of existing user", example = "1") Long id) {
        return orderService.getByBuyerId(id);
    }

    @GetMapping("/buyer")
    @ApiOperation(value = "Returns list of orders made by currently authenticated user.")
    @JsonView(OrderSimple.class)
    public List<Order> getOrdersByCurrentUser() {
        return orderService.getByCurrentUser();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Adds new order to database")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to add new order"),
                           @ApiResponse(code = 422, message = "Order has invalid data")})
    @JsonView(OrderSimple.class)
    public Order addOrder(@Valid @RequestBody @ApiParam(value = "Data of the new order") Order order) {
        return orderService.add(order);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Modifies status of the order")
    @ApiResponses({@ApiResponse(code = 403, message = "Non-manager is trying to modify order status"),
                   @ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
    public void modifyOrderStatus(@RequestBody @ApiParam(value = "Id and new status of the order",
            examples = @Example(@ExampleProperty(value = "{ 'id': 1, 'status': SENT }"))) Order order) {
        orderService.update(order);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Removes a single order by its ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to delete an order"),
                           @ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
    public void deleteOrder(@PathVariable @ApiParam(value = "Unique ID of existing order", example = "1") Long id) {
        orderService.delete(id);
    }

    @InitBinder
    public void addCustomOrderValidator(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new OrderValidator(productService));
    }
}