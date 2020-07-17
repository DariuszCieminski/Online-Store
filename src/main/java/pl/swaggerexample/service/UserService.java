package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.UserDaoImpl;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.User;

import java.util.Collections;
import java.util.List;

@Service
public class UserService
{
	private final UserDaoImpl userDao;
	
	@Autowired
	public UserService(UserDaoImpl userDao) {this.userDao = userDao;}
	
	public List<User> getAllUsers()
	{
		return userDao.getAll();
	}
	
	public User getUserById(Long id)
	{
		return userDao.getById(id).orElseThrow(() -> new NotFoundException("There is no user with id: " + id));
	}
	
	public User getUserByEmail(String email)
	{
		return userDao.getUserByEmail(email).orElse(null);
	}
	
	public User addUser(User user)
	{
		if (user.getRoles() == null || user.getRoles().isEmpty()) user.setRoles(Collections.singleton(Role.USER));
		validateUser(user);
		user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
		
		return userDao.save(user);
	}
	
	public void deleteUser(Long userId)
	{
		User user = getUserById(userId);
		userDao.delete(user);
	}
	
	private void validateUser(User user)
	{
		if (getAllUsers().stream().anyMatch(u -> u.getEmail().equals(user.getEmail())))
			throw new IllegalArgumentException("There is already a user with e-mail address: " + user.getEmail());
		if (user.getEmail().isEmpty()) throw new IllegalArgumentException("E-Mail address cannot be empty!");
		if (user.getName().isEmpty() || user.getSurname().isEmpty())
			throw new IllegalArgumentException("User name or surname cannot be empty!");
		if (user.getPassword().isEmpty()) throw new IllegalArgumentException("User password was not specified!");
		if (user.getRoles() == null || user.getRoles().isEmpty())
			throw new IllegalArgumentException("User needs to have assigned at least 1 role!");
	}
}