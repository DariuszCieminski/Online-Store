package pl.onlinestore.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import pl.onlinestore.model.User;

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