package pl.swaggerexample.validation;

import java.util.Optional;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, User> {

    private final UserService userService;

    @Autowired
    public UniqueEmailValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        Optional<User> user = userService.getUserByEmail(value.getEmail());

        if (user.isPresent() && !user.get().getId().equals(value.getId())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "A user with e-mail address: " + user.get().getEmail() + " already exists.")
                   .addPropertyNode("email").addConstraintViolation();
            return false;
        }

        return true;
    }
}