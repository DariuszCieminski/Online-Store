package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureMockMvc
class UserControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private UserService userService;
	
	private static UserBuilder user = SwaggerTests.user;
	private static UserBuilder manager = SwaggerTests.manager;
	
	@Test
	public void addUserExpectSuccess() throws Exception
	{
		User client = new User("Użytkownik", "Testowy", "test@test.com", "test");
		Address address = new Address("ul. Testowa 1", "01-234", "Testowo");
		client.setAddress(address);
		
		mockMvc.perform(post("/api/users").with(user(user.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void createUserWithoutEmailExpectUnprocessableEntity() throws Exception
	{
		User client = new User("Jan", "Kowalski", "", "moje_haslo");
		mockMvc.perform(post("/api/users").with(user(user.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getUserByValidId() throws Exception
	{
		User client = userService.addUser(new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo"));
		mockMvc.perform(get("/api/users/{id}", client.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(client.getId()));
	}
	
	@Test
	public void getUserByInvalidId() throws Exception
	{
		mockMvc.perform(get("/api/users/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void addAndgetUsersExpectGreaterOrEqualToNumberOfUsersAdded() throws Exception
	{
		int counter = 3;
		for (int i = 0; i < counter; i++)
		{
			userService.addUser(new User("Jan", "Kowalski", "jan.kowalski" + i + "@poczta.pl", "moje_haslo"));
		}
		mockMvc.perform(get("/api/users").with(user(user.build()))).andDo(print()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$", hasSize(Matchers.greaterThanOrEqualTo(counter))));
	}
	
	@Test
	public void deleteUserWithAuthorizationExpectSuccess() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 1L).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteUserWithoutAuthorizationExpectFail() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteUserWithInvalidId() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}