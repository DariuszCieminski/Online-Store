package pl.swaggerexample;

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
import com.fasterxml.jackson.databind.type.CollectionType;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hamcrest.Matchers;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.configuration.CustomRequest;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.util.JsonViews.UserSimple;
import pl.swaggerexample.util.JsonViews.UserDetailed;

@SpringBootTest
@ComponentScan
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomRequest request;

    @BeforeAll
    public void init() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("ALTER SEQUENCE user_sequence RESTART WITH 1").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();

        User user = new User("Jan", "Kowalski", "user@test.pl", "moje_haslo", Collections.singleton(Role.USER));
        User manager = new User("Jan", "Kowalski", "manager@test.pl", "moje_haslo", Collections.singleton(Role.MANAGER));
        userDao.save(user);
        userDao.save(manager);
    }

    @AfterAll
    public void cleanup() {
        userDao.deleteAll();
    }

    @Test
    public void addUserReturnOk() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.USER));
        Address address = new Address("ul. Testowa 1", "01-234", "Testowo");
        user.setAddress(address);

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    public void addUserWithoutNameReturnUnprocessableEntity() throws Exception {
        User user = new User("", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithoutSurnameReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithoutEmailReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));
        user.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithNoPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithTooShortPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "haslo", Collections.singleton(Role.USER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void addUserWithDeveloperRoleWithPermissionReturnUserWithDeveloperRole() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isCreated());

        CollectionType userListCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
        String allUsersJson = mockMvc.perform(get("/api/users")).andReturn().getResponse().getContentAsString();
        List<User> allUsersList = mapper.readValue(allUsersJson, userListCollectionType);
        Optional<User> addedUser = allUsersList.stream().max(Comparator.comparingLong(User::getId));

        assertTrue(addedUser.isPresent());
        assertTrue(addedUser.get().getRoles().contains(Role.DEVELOPER), "Added user doesn't have DEVELOPER role!");
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void addUserWithDeveloperRoleWithoutPermissionReturnUserWithStandardRole() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));

        mockMvc.perform(request.builder(HttpMethod.POST, "/api/users").with(anonymous())
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isCreated());

        CollectionType userListCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
        String allUsersJson = mockMvc.perform(get("/api/users")).andReturn().getResponse().getContentAsString();
        List<User> allUsersList = mapper.readValue(allUsersJson, userListCollectionType);
        Optional<User> addedUser = allUsersList.stream().max(Comparator.comparingLong(User::getId));

        assertTrue(addedUser.isPresent());
        assertFalse(addedUser.get().getRoles().contains(Role.DEVELOPER), "Added user has not allowed DEVELOPER role!");
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void allUsersJsonReturnOk() throws Exception {
        mockMvc.perform(get("/api/users")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void allUsersJsonWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getUserByValidIdReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/1")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getUserWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/1")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getUserByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getUserWithUserDetailedViewShouldContainIdAndRoles() throws Exception {
        String userListJson = mockMvc.perform(get("/api/users/1"))
                                     .andExpect(status().isOk())
                                     .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(UserDetailed.class).readValue(userListJson, User.class);

        assertNotNull(user.getId(), "User ID is null!");
        assertNotNull(user.getRoles(), "User roles are null!");
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getUserWithUserAuthenticationViewShouldNotContainIdAndRoles() throws Exception {
        String userListJson = mockMvc.perform(get("/api/users/1"))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(UserSimple.class).readValue(userListJson, User.class);

        assertNull(user.getId(), "User ID is not null!");
        assertNull(user.getRoles(), "User roles are not null!");
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void addAndgetUsersExpectGreaterOrEqualToNumberOfUsersAdded() throws Exception {
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
               .andExpect(jsonPath("$", hasSize(Matchers.greaterThanOrEqualTo(counter))));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserSurnameReturnOk() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setSurname("Nowak");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.surname").value(parsedUser.getSurname()));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithInvalidIdReturnNotFound() throws Exception {
        User user = new User("Jan", "Kowalski", "testowy.email@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
        user.setId(333L);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(user))).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithEmptyNameReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setName(null);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithEmptySurnameReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setSurname(null);

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("surname"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithInvalidEmailAndPasswordReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setEmail("testowy.email");
        parsedUser.setPassword("hasło");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors", hasItems(hasEntry("field", "email"), hasEntry("field", "password"))));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users")
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors",
                                   hasItems(hasEntry("field", "address.street"), hasEntry("field", "address.postCode"))));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateUserWithoutAuthorizationReturnForbidden() throws Exception {
        String userJson = mockMvc.perform(get("/api/users/1"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(userJson, User.class);
        parsedUser.setEmail("testowy.email@poczta.pl");

        mockMvc.perform(request.builder(HttpMethod.PUT, "/api/users").with(user("user"))
               .content(mapper.writeValueAsString(parsedUser))).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteUserWithAuthorizationReturnNoContent() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 1L)).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void deleteUserWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 1L)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteUserWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/users/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }
}