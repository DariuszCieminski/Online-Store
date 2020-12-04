package pl.swaggerexample.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.service.OrderService;
import pl.swaggerexample.service.ProductService;
import pl.swaggerexample.util.JsonViews;
import pl.swaggerexample.validation.OrderValidator;

@RestController
@RequestMapping("/api/orders")
@Api(description = "Endpoints for getting, adding and removing orders that users make.")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    @ApiOperation(value = "Returns list of all made orders")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get orders")})
    @JsonView(JsonViews.OrderDetailed.class)
    public List<Order> getOrders() {
        return orderService.getAll();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Returns a single order by its ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get an order"),
                           @ApiResponse(code = 404, message = "Order with specified ID doesn't exist")})
    @JsonView(JsonViews.OrderDetailed.class)
    public Order getOrderById(@PathVariable @ApiParam(value = "Unique ID of existing order", example = "1") Long id) {
        return orderService.getById(id);
    }

    @GetMapping("/buyer/{id}")
    @ApiOperation(value = "Returns list of orders made by user with given ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get orders of another user"),
                           @ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
    @JsonView(JsonViews.OrderSimple.class)
    public List<Order> getOrdersByBuyerId(
        @PathVariable @ApiParam(value = "Unique ID of existing user", example = "1") Long id) {
        return orderService.getOrdersByBuyerId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Adds new order to database")
    @ApiResponses(value = {@ApiResponse(code = 422, message = "Order has invalid data")})
    @JsonView(JsonViews.OrderSimple.class)
    public Order addOrder(@Valid @RequestBody @ApiParam(value = "Data of the new order") Order order) {
        return orderService.add(order);
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