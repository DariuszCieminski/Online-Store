package pl.swaggerexample.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;
import pl.swaggerexample.util.ValidationGroups;

import javax.validation.Valid;
import javax.validation.groups.Default;
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
		return userService.getAll();
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Returns single registered customer by his ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
	public User getUser(@PathVariable @ApiParam(value = "Unique ID of existing customer", example = "1") Long id)
	{
		return userService.getById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Adds new user to the database")
	@ApiResponses(value = {@ApiResponse(code = 422, message = "User has invalid data")})
	public User createUser(@Validated({Default.class, ValidationGroups.UserCreation.class}) @RequestBody @ApiParam(value = "Data of the new user") User user)
	{
		return userService.add(user);
	}
	
	@PutMapping
	@ApiOperation(value = "Updates an existing user")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "User with specified ID doesn't exist"), @ApiResponse(code = 422, message = "Updated user has invalid data")})
	public User updateUser(@Valid @RequestBody @ApiParam(value = "Updated data of the existing user") User user)
	{
		return userService.update(user);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Removes a single user by his ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
	public void deleteUser(@PathVariable @ApiParam(value = "Unique ID of existing user", example = "1") Long id)
	{
		userService.delete(id);
	}
}