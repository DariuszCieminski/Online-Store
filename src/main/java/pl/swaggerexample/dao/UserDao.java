package pl.swaggerexample.dao;

import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.User;

import java.util.Optional;

public interface UserDao extends CrudRepository<User, Long>
{
	Optional<User> getUserByEmail(String email);
}