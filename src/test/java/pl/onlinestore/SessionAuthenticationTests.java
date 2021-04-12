package pl.onlinestore;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.onlinestore.configuration.CustomRequest;
import pl.onlinestore.dao.UserDao;
import pl.onlinestore.model.User;
import pl.onlinestore.model.enums.Role;

@SpringBootTest
@ActiveProfiles("session")
@ComponentScan
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionAuthenticationTests {

    private static final String LOGIN_TEMPLATE = "{\"email\":\"%s\",\"password\":\"%s\"}";
    private static final User USER = new User("Test", "User", "user@test.pl",
                                              "{noop}myP@ssw0rd", Collections.singleton(Role.USER));
    private static final User DEV = new User("Test", "User", "dev@test.pl",
                                             "{noop}myP@ssw0rd", Collections.singleton(Role.DEVELOPER));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CustomRequest request;

    @BeforeAll
    void init() {
        userDao.save(USER);
        userDao.save(DEV);

        USER.setPassword("myP@ssw0rd");
        DEV.setPassword("myP@ssw0rd");
    }

    @AfterAll
    void cleanup() {
        userDao.deleteAll();
    }

    @Test
    void sessionLoginShouldReturnOk() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
               .andExpect(status().isOk())
               .andExpect(authenticated())
               .andExpect(jsonPath("$.name").exists())
               .andExpect(jsonPath("$.surname").exists())
               .andExpect(jsonPath("$.email").exists())
               .andExpect(jsonPath("$.address").value(anyOf(notNullValue(), nullValue())))
               .andExpect(jsonPath("$.roles").isArray())
               .andExpect(jsonPath("$.roles").isNotEmpty());
    }

    @Test
    void sessionLoginInvalidPasswordShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), "wrong_password")))
               .andExpect(status().isUnauthorized())
               .andExpect(unauthenticated())
               .andExpect(cookie().doesNotExist("session_id"));
    }

    @Test
    void sessionLogoutShouldDeleteSessionAndCsrfCookies() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.POST, "/login")
               .content(String.format(LOGIN_TEMPLATE, USER.getEmail(), USER.getPassword())))
               .andExpect(status().isOk())
               .andExpect(authenticated());

        mockMvc.perform(request.builder(HttpMethod.POST, "/logout"))
               .andExpect(status().isNoContent())
               .andExpect(unauthenticated())
               .andExpect(cookie().value("session_id", nullValue()))
               .andExpect(cookie().value("XSRF-TOKEN", nullValue()));
    }

    @Test
    @WithUserDetails("user@test.pl")
    void pingServerAsUserShouldReturnOkWithUserInformation() throws Exception {
        mockMvc.perform(get("/api/util/ping"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").exists())
               .andExpect(jsonPath("$.surname").exists())
               .andExpect(jsonPath("$.email").exists())
               .andExpect(jsonPath("$.address").value(anyOf(notNullValue(), nullValue())))
               .andExpect(jsonPath("$.roles").isArray())
               .andExpect(jsonPath("$.roles").isNotEmpty());
    }

    @Test
    void pingServerAnonymouslyShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/util/ping"))
               .andExpect(status().isUnauthorized());
    }
}