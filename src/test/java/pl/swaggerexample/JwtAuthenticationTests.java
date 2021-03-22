package pl.swaggerexample;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.configuration.CustomRequest;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;

@SpringBootTest
@ActiveProfiles("jwt")
@ComponentScan
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtAuthenticationTests {

    private static final String LOGIN_TEMPLATE = "{\"email\":\"%s\",\"password\":\"%s\"}";
    private static final String REFRESH_TOKEN_TEMPLATE = "{\"access_token\":%s,\"refresh_token\":%s}";
    private static final User USER = new User("Test", "User", "user@test.pl",
                                              "{noop}myP@ssw0rd", Collections.singleton(Role.USER));
    private static final User DEV = new User("Test", "User", "dev@test.pl",
                                             "{noop}myP@ssw0rd", Collections.singleton(Role.DEVELOPER));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomRequest request;

    @BeforeAll
    public void init() {
        userDao.save(USER);
        userDao.save(DEV);

        USER.setPassword("myP@ssw0rd");
        DEV.setPassword("myP@ssw0rd");
    }

    @AfterAll
    public void cleanup() {
        userDao.deleteAll();
    }

    @Test
    public void jwtLoginShouldReturnOk() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.user").exists())
               .andExpect(jsonPath("$.access_token").exists())
               .andExpect(jsonPath("$.refresh_token").exists());
    }

    @Test
    public void jwtLoginInvalidPasswordShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), "wrong_password")))
               .andExpect(status().isUnauthorized())
               .andExpect(cookie().doesNotExist("swagger_id"));
    }

    @ParameterizedTest
    @MethodSource("getLoginDataList")
    public void loginWithInvalidDataShouldReturnUnauthorized(String loginData) throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(loginData))
               .andExpect(status().isUnauthorized());
    }

    private List<String> getLoginDataList() throws Exception {
        List<String> loginDataList = new ArrayList<>();
        ObjectNode loginData = mapper.createObjectNode();

        //only email
        loginData.put("email", USER.getEmail());
        loginDataList.add(mapper.writeValueAsString(loginData));

        //email is null
        loginData.set("email", null);
        loginDataList.add(mapper.writeValueAsString(loginData));

        //only password
        loginData.removeAll();
        loginData.put("password", USER.getPassword());
        loginDataList.add(mapper.writeValueAsString(loginData));

        //empty strings
        loginData.put("password", "");
        loginData.put("email", "");
        loginDataList.add(mapper.writeValueAsString(loginData));

        return loginDataList;
    }

    @Test
    public void loginAsDeveloperShouldCreateSwaggerCookie() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, DEV.getEmail(), DEV.getPassword())))
               .andExpect(status().isOk())
               .andExpect(cookie().exists("swagger_id"))
               .andExpect(cookie().httpOnly("swagger_id", true))
               .andExpect(cookie().value("swagger_id", notNullValue()));
    }

    @Test
    public void loginAsUserShouldNotCreateSwaggerCookie() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
               .andExpect(status().isOk())
               .andExpect(cookie().doesNotExist("swagger_id"));
    }

    @Test
    public void refreshAccessTokenShouldReturnNewToken() throws Exception {
        String loginResultJson = mockMvc.perform(request.builder(HttpMethod.POST, "/login")
                                        .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        String requestBody = String.format(REFRESH_TOKEN_TEMPLATE, mapper.readTree(loginResultJson).get("access_token"),
                                           mapper.readTree(loginResultJson).get("refresh_token"));

        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .header("Authorization", "Bearer " + mapper.readTree(loginResultJson).get("access_token"))
               .content(requestBody))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.access_token").exists());
    }

    @Test
    public void refreshAccessTokenWithoutRefreshTokenShouldReturnUnauthorized() throws Exception {
        String loginResultJson = mockMvc.perform(request.builder(HttpMethod.POST, "/login")
                                        .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        String requestBody = String.format(REFRESH_TOKEN_TEMPLATE, mapper.readTree(loginResultJson).get("access_token"), "");

        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .header("Authorization", "Bearer " + mapper.readTree(loginResultJson).get("access_token"))
               .content(requestBody))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void refreshAccessTokenWithoutAuthorizationHeaderShouldReturnUnauthorized() throws Exception {
        String loginResultJson = mockMvc.perform(request.builder(HttpMethod.POST, "/login")
                                        .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        String requestBody = String.format(REFRESH_TOKEN_TEMPLATE, mapper.readTree(loginResultJson).get("access_token"),
                                           mapper.readTree(loginResultJson).get("refresh_token"));

        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(requestBody))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void jwtLogoutShouldDeleteSwaggerCookie() throws Exception {
        MockHttpServletResponse loginResponse = mockMvc.perform(request.builder(HttpMethod.POST, "/login")
                                                       .content(String.format(LOGIN_TEMPLATE, DEV.getEmail(), DEV.getPassword())))
                                                       .andExpect(status().isOk())
                                                       .andExpect(cookie().exists("swagger_id"))
                                                       .andExpect(cookie().value("swagger_id", notNullValue()))
                                                       .andReturn().getResponse();

        mockMvc.perform(request.builder(HttpMethod.POST, "/logout")
               .cookie(loginResponse.getCookie("swagger_id")))
               .andExpect(status().isNoContent())
               .andExpect(cookie().value("swagger_id", nullValue()));
    }

    @Test
    @WithMockUser
    public void pingServerShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/util/ping"))
               .andExpect(status().isNotFound());
    }
}