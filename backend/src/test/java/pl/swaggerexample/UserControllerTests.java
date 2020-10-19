package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.User;
import pl.swaggerexample.security.JwtAuthorizationFilter;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class, properties = "spring.jpa.properties.javax.persistence.validation.mode=ddl")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DirtiesContext
class UserControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static final UserBuilder USER = SwaggerTests.USER;
	private static final UserBuilder MANAGER = SwaggerTests.MANAGER;
	
	private String getAccessToken() throws Exception
	{
		String loginContent = String.format(JwtTests.LOGIN_TEMPLATE, "jan.kowalski@poczta.pl", "moje_haslo");
		String loginResponse = mockMvc.perform(post("/login").content(loginContent)).andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		
		return mapper.readTree(loginResponse).get("access_token").textValue();
	}
	
	@BeforeAll
	public void init() throws Exception
	{
		User client = new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
		mockMvc.perform(post("/api/users").with(user(MANAGER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
	}
	
	@Test
	public void addUserReturnOk() throws Exception
	{
		User client = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.USER));
		Address address = new Address("ul. Testowa 1", "01-234", "Testowo");
		client.setAddress(address);
		
		mockMvc.perform(post("/api/users").with(user(USER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addUserWithoutEmailReturnUnprocessableEntity() throws Exception
	{
		User client = new User("Jan", "Kowalski", "", "moje_haslo", Collections.singleton(Role.USER));
		mockMvc.perform(post("/api/users").with(user(USER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getUserByValidIdReturnOk() throws Exception
	{
		mockMvc.perform(get("/api/users/1").with(user(MANAGER.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(1));
	}
	
	@Test
	public void getUserWithoutPermissionReturnForbidden() throws Exception
	{
		User user = new User("Jan", "Nowak", "jan.nowak@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
		String addUserResponse = mockMvc.perform(post("/api/users").with(user(MANAGER.build())).content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
		String accessToken = getAccessToken();
		
		mockMvc.perform(get("/api/users/{id}", mapper.readTree(addUserResponse).get("id").longValue()).header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX + accessToken)).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void getUserByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/users/{id}", 999L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void addAndgetUsersExpectGreaterOrEqualToNumberOfUsersAdded() throws Exception
	{
		int counter = 3;
		for (int i = 1; i <= counter; i++)
		{
			User client = new User("Jan", "Kowalski", "jan.kowalski" + i + "@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
			mockMvc.perform(post("/api/users").with(user(MANAGER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
		mockMvc.perform(get("/api/users").with(user(MANAGER.build()))).andDo(print()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$", hasSize(Matchers.greaterThanOrEqualTo(counter))));
	}
	
	@Test
	public void updateUserReturnOk() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get("/api/users/1").with(user(MANAGER.build()))).andExpect(status().isOk()).andReturn();
		User client = mapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);
		client.setSurname("Nowak");
		
		mockMvc.perform(put("/api/users").with(user(USER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.surname").value(client.getSurname()));
	}
	
	@Test
	public void updateUserReturnUnprocessableEntity() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get("/api/users/1").with(user(MANAGER.build()))).andExpect(status().isOk()).andReturn();
		User client = mapper.readValue(mvcResult.getResponse().getContentAsString(), User.class);
		client.setEmail("testowy.email");
		client.setPassword("hasło");
		
		mockMvc.perform(put("/api/users").with(user(USER.build())).content(mapper.writeValueAsString(client)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.errors").isArray()).andExpect(jsonPath("$.errors", hasSize(2)));
	}
	
	@Test
	public void deleteUserWithAuthorizationReturnNoContent() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 1L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteUserWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 1L).with(user(USER.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteUserWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/users/{id}", 999L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}