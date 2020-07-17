package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

@ApiModel(description = "Shop's customer")
public class User
{
	@ApiModelProperty(value = "A unique user identifier", readOnly = true, example = "1")
	private Long id;
	@ApiModelProperty(value = "User's name", required = true, example = "John", position = 1)
	private String name;
	@ApiModelProperty(value = "User's surname", required = true, example = "Smith", position = 2)
	private String surname;
	@ApiModelProperty(notes = "Home address of the user", position = 4)
	private Address address;
	@ApiModelProperty(value = "E-Mail address of the user", required = true, example = "john.smith@myemail.com", position = 3)
	private String email;
	@ApiModelProperty(value = "Password for user account. Will be encrypted after account creation.", required = true, example = "myP@ssw0rd", position = 5)
	private String password;
	@ApiModelProperty(value = "All roles that user has. Must not be empty or null.", required = true, position = 6)
	private Set<Role> roles;
	
	public User(String name, String surname, String email, String password)
	{
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public void setSurname(String surname)
	{
		this.surname = surname;
	}
	
	public Address getAddress()
	{
		return address;
	}
	
	public void setAddress(Address address)
	{
		this.address = address;
	}
	
	public String getEmail()
	{
		return email;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public Set<Role> getRoles()
	{
		return roles;
	}
	
	public void setRoles(Set<Role> roles)
	{
		this.roles = roles;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof User)) return false;
		User user = (User) o;
		return getId().equals(user.getId());
	}
}