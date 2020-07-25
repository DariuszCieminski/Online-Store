package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.User;
import pl.swaggerexample.util.ValidationGroups;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements EntityService<User>
{
	private final UserDao userDao;
	
	@Autowired
	public UserService(UserDao userDao)
	{
		this.userDao = userDao;
	}
	
	public Optional<User> getUserByEmail(String email)
	{
		return userDao.getUserByEmail(email);
	}
	
	@Override
	public User getById(Long id)
	{
		return userDao.findById(id).orElseThrow(() -> new NotFoundException("There is no user with id: " + id));
	}
	
	@Override
	public List<User> getAll()
	{
		List<User> users = new ArrayList<>();
		userDao.findAll().forEach(users::add);
		return users;
	}
	
	@Override
	public User add(@Validated({Default.class, ValidationGroups.UserCreation.class}) User object)
	{
		object.setPassword(BCrypt.hashpw(object.getPassword(), BCrypt.gensalt()));
		return userDao.save(object);
	}
	
	@Override
	public User update(@Valid User object)
	{
		User user = getById(object.getId());
		if (object.getPassword() != null) object.setPassword(BCrypt.hashpw(object.getPassword(), BCrypt.gensalt()));
		else object.setPassword(user.getPassword());
		return userDao.save(object);
	}
	
	@Override
	public void delete(Long id)
	{
		User user = getById(id);
		userDao.delete(user);
	}
}