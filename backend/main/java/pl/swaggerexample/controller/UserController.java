package pl.swaggerexample.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import javax.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;
import pl.swaggerexample.util.JsonViews;
import pl.swaggerexample.util.ValidationGroups;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/users")
@Api(description = "Endpoints for getting, creating and removing shop customers.")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ApiOperation(value = "Returns list of all registered customers")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get all users")})
    @JsonView(JsonViews.UserDetailed.class)
    public List<User> getUsers() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Returns single registered customer by his ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to get a user"),
                           @ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
    @JsonView(JsonViews.UserDetailed.class)
    public User getUserById(@PathVariable @ApiParam(value = "Unique ID of existing customer", example = "1") Long id) {
        return userService.getById(id);
    }

    @GetMapping("/currentuser")
    @ApiOperation(value = "Returns currently logged user")
    @JsonView(JsonViews.UserAuthentication.class)
    public User getCurrentUser(@ApiIgnore Authentication authentication) {
        return userService.getById((Long) authentication.getDetails());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Adds new user to the database")
    @ApiResponses(value = {@ApiResponse(code = 422, message = "User has invalid data")})
    @JsonView(JsonViews.UserAuthentication.class)
    public User createUser(@Validated({Default.class, ValidationGroups.UserCreation.class})
                           @RequestBody @ApiParam(value = "Data of the new user") User user) {
        return userService.add(user);
    }

    @PutMapping
    @ApiOperation(value = "Updates an existing user")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to update user"),
                           @ApiResponse(code = 404, message = "User with specified ID doesn't exist"),
                           @ApiResponse(code = 422, message = "Updated user has invalid data")})
    public User updateUser(@Valid @RequestBody @ApiParam(value = "Updated data of the existing user") User user) {
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Removes a single user by his ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to delete user"),
                           @ApiResponse(code = 404, message = "User with specified ID doesn't exist")})
    public void deleteUser(@PathVariable @ApiParam(value = "Unique ID of existing user", example = "1") Long id) {
        userService.delete(id);
    }
}