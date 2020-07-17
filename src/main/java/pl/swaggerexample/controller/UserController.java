package pl.swaggerexample.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Api(description = "Endpoints for getting, creating and removing shop customers.")
public class UserController
{
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService)
	{
		this.userService = userService;
	}
	
	@GetMapping
	@ApiOperation(value = "Returns list of all registered customers")
	public List<User> getUsers()
	{
		return userService.getAllUsers();
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Returns single registered customer by his ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
	public User getUser(@PathVariable @ApiParam(value = "Unique ID of existing customer", example = "1") Long id)
	{
		return userService.getUserById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Adds new user to the database")
	@ApiResponses(value = {@ApiResponse(code = 422, message = "User has invalid data")})
	public User createUser(@RequestBody @ApiParam(value = "Data of the new customer") User user)
	{
		return userService.addUser(user);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Removes a single user by his ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
	public void deleteUser(@PathVariable @ApiParam(value = "Unique ID of existing customer", example = "1") Long id)
	{
		userService.deleteUser(id);
	}
}