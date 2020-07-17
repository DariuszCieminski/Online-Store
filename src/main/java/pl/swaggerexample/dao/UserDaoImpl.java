package pl.swaggerexample.dao;

import org.springframework.stereotype.Repository;
import pl.swaggerexample.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements Dao<User>
{
	private List<User> users = new ArrayList<>();
	
	public Optional<User> getUserByEmail(String email)
	{
		return users.stream().filter(user -> user.getEmail().equals(email)).findFirst();
	}
	
	@Override
	public Optional<User> getById(Long id)
	{
		return users.stream().filter(user -> user.getId().equals(id)).findFirst();
	}
	
	@Override
	public List<User> getAll()
	{
		return users;
	}
	
	@Override
	public User save(User user)
	{
		if (!users.isEmpty())
		{
			User u = users.get(users.size() - 1);
			user.setId(u.getId() + 1);
		}
		else user.setId(1L);
		users.add(user);
		
		return user;
	}
	
	@Override
	public User update(User user)
	{
		return null;
	}
	
	@Override
	public void delete(User user)
	{
		users.remove(user);
	}
}