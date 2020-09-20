package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.User;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class, properties = "spring.jpa.properties.javax.persistence.validation.mode=ddl")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Rollback
public class JwtTests
{
	private static final User user = new User("Test", "User", "testuser@example.com", "myP@ssw0rd", Collections.singleton(Role.USER));
	private static final User dev = new User("Test", "User", "testdev@example.com", "myP@ssw0rd", Collections.singleton(Role.DEVELOPER));
	private static final String loginTemplate = "{\"email\":\"%s\",\"password\":\"%s\"}";
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper mapper;
	
	@BeforeAll
	public void init() throws Exception
	{
		mockMvc.perform(post("/api/users").content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		mockMvc.perform(post("/api/users").content(mapper.writeValueAsString(dev)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
	}
	
	@Test
	public void loginShouldReturnOk() throws Exception
	{
		mockMvc.perform(post("/login").content(String.format(loginTemplate, user.getEmail(), user.getPassword())).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.user").exists()).andExpect(jsonPath("$.access_token").exists()).andExpect(jsonPath("$.refresh_token").exists());
	}
	
	@Test
	public void loginInvalidPasswordShouldReturnUnauthorized() throws Exception
	{
		mockMvc.perform(post("/login").content(String.format(loginTemplate, user.getEmail(), "wrong_password")).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized()).andExpect(status().reason("Unauthorized"));
	}
	
	@Test
	public void loginAsDeveloperShouldCreateSwaggerCookie() throws Exception
	{
		mockMvc.perform(post("/login").content(String.format(loginTemplate, dev.getEmail(), dev.getPassword())).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(cookie().exists("swagger_id")).andExpect(cookie().httpOnly("swagger_id", true));
	}
}