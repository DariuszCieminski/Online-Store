package pl.swaggerexample.validation;

import org.springframework.beans.factory.annotation.Autowired;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, User>
{
	private final UserService userService;
	
	@Autowired
	public UniqueEmailValidator(UserService userService)
	{
		this.userService = userService;
	}
	
	@Override
	public boolean isValid(User value, ConstraintValidatorContext context)
	{
		Optional<User> user = userService.getUserByEmail(value.getEmail());
		
		if (user.isPresent())
		{
			if (!user.get().getId().equals(value.getId()))
			{
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("There is already a user with e-mail address: " + user.get().getEmail())
						.addPropertyNode("email").addConstraintViolation();
				return false;
			}
		}
		
		return true;
	}
}