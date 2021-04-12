package pl.onlinestore;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.onlinestore.configuration.CustomRequest;
import pl.onlinestore.dao.UserDao;
import pl.onlinestore.model.Address;
import pl.onlinestore.model.User;
import pl.onlinestore.model.enums.Role;
import pl.onlinestore.service.UserService;
import pl.onlinestore.util.JsonViews.UserDetailed;
import pl.onlinestore.util.JsonViews.UserSimple;

@SpringBootTest
@ComponentScan
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@Transactional
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomRequest request;

    @BeforeAll
    void init() {
        User user = new User("Jan", "Kowalski", "user@test.pl", "moje_haslo", Collections.singleton(Role.USER));
        User manager = new User("Jan", "Kowalski", "manager@test.pl", "moje_haslo", Collections.singleton(Role.MANAGER));
        user.setId(1L);
        manager.setId(2L);

        userDao.save(user);
        userDao.save(manager);
    }

    @AfterAll
    void cleanup() {
        userDao.deleteAll();
    }

    @Test
    void addUserReturnOk() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.USER));
        Address address = new Address("ul. Testowa 1", "01-234", "Testowo");
        user.setAddress(address);

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    void addUserWithoutNameReturnUnprocessableEntity() throws Exception {
        User user = new User("", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addUserWithoutSurnameReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addUserWithoutEmailReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));
        user.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addUserWithNoPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void addUserWithTooShortPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void addUserWithDeveloperRoleWithPermissionReturnUserWithRoleDeveloper() {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));
        user = userService.add(user);

        assertTrue(user.getRoles().contains(Role.DEVELOPER), "Added user doesn't have DEVELOPER role!");
    }

    @Test
    void addUserWithDeveloperRoleWithoutPermissionReturnUserWithRoleUser() {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));
        user = userService.add(user);

        assertFalse(user.getRoles().contains(Role.DEVELOPER), "Added user has not allowed DEVELOPER role!");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAllUsersReturnOk() throws Exception {
        mockMvc.perform(get("/api/users")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @WithMockUser
    void getAllUsersWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getUserByValidIdReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/1")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void getUserWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/1")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getUserByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getUserWithUserDetailedViewShouldContainIdAndRoles() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(UserDetailed.class).readValue(userJson, User.class);

        assertNotNull(user.getId(), "User ID is null!");
        assertNotNull(user.getRoles(), "User roles are null!");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getUserWithUserAuthenticationViewShouldNotContainIdAndRoles() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(UserSimple.class).readValue(userJson, User.class);

        assertNull(user.getId(), "User ID is not null!");
        assertNull(user.getRoles(), "User roles are not null!");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void addAndgetUsersExpectGreaterOrEqualToNumberOfUsersAdded() throws Exception {
        int counter = 3;
        for (int i = 1; i <= counter; i++) {
            User user = new User("Jan", "Kowalski", "jan.kowalski" + i + "@poczta.pl", "moje_haslo",
                                 Collections.singleton(Role.USER));

            mockMvc.perform(request.builder(HttpMethod.POST, "/api/users").with(anonymous())
                   .content(mapper.writeValueAsString(user)))
                   .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/users")).andDo(print())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(counter))));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserSurnameReturnOk() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setSurname("Nowak");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.surname").value(readUser.getSurname()));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithInvalidIdReturnNotFound() throws Exception {
        User user = new User("Jan", "Kowalski", "testowy.email@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
        user.setId(333L);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithEmptyNameReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setName(null);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithEmptySurnameReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setSurname(null);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("surname"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithInvalidEmailAndPasswordReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setEmail("testowy.email");
        readUser.setPassword("hasło");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors", hasItems(hasEntry("field", "email"), hasEntry("field", "password"))));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors",
                                   hasItems(hasEntry("field", "address.street"), hasEntry("field", "address.postCode"))));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateUserWithoutAuthorizationReturnForbidden() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                 .andExpect(status().isOk())
                                 .andReturn().getResponse().getContentAsString();

        User readUser = mapper.readValue(userJson, User.class);
        readUser.setEmail("testowy.email@poczta.pl");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users").with(user("user"))
               .content(mapper.writeValueAsString(readUser))).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteUserWithAuthorizationReturnNoContent() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 1L)).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteUserWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 1L)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deleteUserWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }
}