package pl.swaggerexample.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.User;

public interface UserDao extends CrudRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> getUserByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = "roles")
    Iterable<User> findAll();
}