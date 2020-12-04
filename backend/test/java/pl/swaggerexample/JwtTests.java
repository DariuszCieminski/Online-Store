package pl.swaggerexample;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.security.JwtAuthorizationFilter;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtTests {

    private static final String LOGIN_TEMPLATE = "{\"email\":\"%s\",\"password\":\"%s\"}";
    private static final String REFRESH_TEMPLATE = "{\"access_token\":%s,\"refresh_token\":%s}";
    private static final User USER = new User("Test", "User", "testuser@example.com", "myP@ssw0rd",
                                              Collections.singleton(Role.USER));
    private static final User DEV = new User("Test", "User", "testdev@example.com", "myP@ssw0rd",
                                             Collections.singleton(Role.DEVELOPER));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    public void init() throws Exception {
        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(USER)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
               .with(user(SwaggerTests.MANAGER.build()))
               .content(mapper.writeValueAsString(DEV)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());
    }

    @Test
    public void loginShouldReturnOk() throws Exception {
        mockMvc.perform(post("/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.user").exists())
               .andExpect(jsonPath("$.access_token").exists())
               .andExpect(jsonPath("$.refresh_token").exists());
    }

    @Test
    public void loginInvalidPasswordShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), "wrong_password")).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginAsDeveloperShouldCreateSwaggerCookie() throws Exception {
        mockMvc.perform(post("/login")
               .content(String.format(LOGIN_TEMPLATE, DEV.getEmail(), DEV.getPassword())).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(cookie().exists("swagger_id"))
               .andExpect(cookie().httpOnly("swagger_id", true));
    }

    @Test
    public void loginAsUserShouldNotCreateSwaggerCookie() throws Exception {
        mockMvc.perform(post("/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(cookie().doesNotExist("swagger_id"));
    }

    @Test
    public void refreshAccessTokenShouldReturnNewToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                                       .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword()))
                                       .contentType(MediaType.APPLICATION_JSON))
                                       .andExpect(status().isOk())
                                       .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String refreshContent = String.format(REFRESH_TEMPLATE, mapper.readTree(loginResponseBody).get("access_token"),
                                              mapper.readTree(loginResponseBody).get("refresh_token"));

        mockMvc.perform(post("/login")
               .header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX
                                                          + mapper.readTree(loginResponseBody).get("access_token"))
               .content(refreshContent).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.access_token").exists());
    }

    @Test
    public void refreshAccessTokenWithoutRefreshTokenShouldReturnUnauthorized() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                                       .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword()))
                                       .contentType(MediaType.APPLICATION_JSON))
                                       .andExpect(status().isOk())
                                       .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String refreshContent = String.format(REFRESH_TEMPLATE, mapper.readTree(loginResponseBody).get("access_token"), "");

        mockMvc.perform(post("/login")
               .header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX
                                                           + mapper.readTree(loginResponseBody).get("access_token"))
               .content(refreshContent).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void refreshAccessTokenWithoutAuthorizationHeaderShouldReturnUnauthorized() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                                       .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword()))
                                       .contentType(MediaType.APPLICATION_JSON))
                                       .andExpect(status().isOk())
                                       .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String refreshContent = String.format(REFRESH_TEMPLATE, mapper.readTree(loginResponseBody).get("access_token"),
                                              mapper.readTree(loginResponseBody).get("refresh_token"));

        mockMvc.perform(post("/login")
               .content(refreshContent).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
    }
}