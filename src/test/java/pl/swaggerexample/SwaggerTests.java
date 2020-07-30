package pl.swaggerexample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.model.Role;

import static org.springframework.security.core.userdetails.User.builder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureMockMvc
public class SwaggerTests
{
	public static UserBuilder user = builder().username("user").password("user").roles(Role.USER.name());
	public static UserBuilder manager = builder().username("manager").password("manager").roles(Role.MANAGER.name());
	public static UserBuilder developer = builder().username("dev").password("dev").roles(Role.DEVELOPER.name());
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void openSwaggerWithAuthorizationReturnOk() throws Exception
	{
		mockMvc.perform(get("/swagger-ui.html").with(user(developer.build()))).andExpect(status().isOk());
		mockMvc.perform(get("/v2/api-docs").with(user(developer.build()))).andExpect(status().isOk());
	}
	
	@Test
	public void openSwaggerWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(get("/swagger-ui.html").with(user(user.build()))).andExpect(status().isForbidden());
		mockMvc.perform(get("/v2/api-docs").with(user(user.build()))).andExpect(status().isForbidden());
	}
}