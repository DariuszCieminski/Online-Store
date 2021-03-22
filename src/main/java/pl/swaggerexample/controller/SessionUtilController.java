package pl.swaggerexample.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.swaggerexample.model.User;
import pl.swaggerexample.security.AuthenticatedUser;
import pl.swaggerexample.util.JsonViews.UserDetailed;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Profile("session")
@RequestMapping("/api/util")
@Api(tags = "Session util controller", description = "This controller is available only when 'session' profile is active."
                                                     + " Provides various utility endpoints.")
public class SessionUtilController {

    @GetMapping("/ping")
    @ApiOperation(value = "Pings server to extend session validity and returns information about authenticated user.")
    @ApiResponses(@ApiResponse(code = 401, message = "User has not authenticated yet or his session has already expired."))
    @JsonView(UserDetailed.class)
    public User ping(@ApiIgnore Authentication authentication) {
        return ((AuthenticatedUser) authentication.getPrincipal()).getUser();
    }
}