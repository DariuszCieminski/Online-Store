package pl.swaggerexample;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.util.JsonViews;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DirtiesContext
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final Authentication USER = new UsernamePasswordAuthenticationToken(
        SwaggerTests.USER.build().getUsername(),
        SwaggerTests.USER.build().getPassword(),
        SwaggerTests.USER.build().getAuthorities());

    private static final Authentication MANAGER = new UsernamePasswordAuthenticationToken(
        SwaggerTests.MANAGER.build().getUsername(),
        SwaggerTests.MANAGER.build().getPassword(),
        SwaggerTests.MANAGER.build().getAuthorities());

    @BeforeAll
    public void init() throws Exception {
        User user = new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());
    }

    @Test
    public void addUserReturnOk() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.USER));
        Address address = new Address("ul. Testowa 1", "01-234", "Testowo");
        user.setAddress(address);

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    public void addUserWithoutNameReturnUnprocessableEntity() throws Exception {
        User user = new User("", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithoutSurnameReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithoutEmailReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "", "moje_haslo", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "moje_haslo", Collections.singleton(Role.USER));
        user.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithNoPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithTooShortPasswordReturnUnprocessableEntity() throws Exception {
        User user = new User("Jan", "Kowalski", "test@test.com", "haslo", Collections.singleton(Role.USER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void addUserWithDeveloperRoleWithPermissionReturnOk() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));

        mockMvc.perform(post("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        String getAllUsers = mockMvc.perform(get("/api/users").with(authentication(MANAGER)))
                                    .andReturn().getResponse().getContentAsString();
        CollectionType userListCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
        List<User> allUserList = mapper.readValue(getAllUsers, userListCollectionType);
        Optional<User> addedUser = allUserList.stream().max(Comparator.comparingLong(User::getId));

        Assertions.assertTrue(addedUser.isPresent());
        Assertions.assertTrue(addedUser.get().getRoles().contains(Role.DEVELOPER), "Added user doesn't have DEVELOPER role!");
    }

    @Test
    public void addUserWithDeveloperRoleWithoutPermissionReturnUserWithStandardRole() throws Exception {
        User user = new User("Użytkownik", "Testowy", "test@test.com", "test-test", Collections.singleton(Role.DEVELOPER));

        mockMvc.perform(post("/api/users")
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        String getAllUsers = mockMvc.perform(get("/api/users").with(authentication(MANAGER)))
                                    .andReturn().getResponse().getContentAsString();
        CollectionType userListCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, User.class);
        List<User> allUserList = mapper.readValue(getAllUsers, userListCollectionType);
        Optional<User> addedUser = allUserList.stream().max(Comparator.comparingLong(User::getId));

        Assertions.assertTrue(addedUser.isPresent());
        Assertions.assertFalse(addedUser.get().getRoles().contains(Role.DEVELOPER),
                      "Added user has not allowed DEVELOPER role!");
    }

    @Test
    public void getAllUsersReturnOk() throws Exception {
        mockMvc.perform(get("/api/users").with(authentication(MANAGER))).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void getAllUsersWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users").with(authentication(USER))).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    public void getUserByValidIdReturnOk() throws Exception {
        mockMvc.perform(get("/api/users/1").with(authentication(MANAGER))).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void getUserWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/1").with(authentication(USER))).andDo(print()).andExpect(status().isForbidden());
    }

    @Test
    public void getUserByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L).with(authentication(MANAGER))).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    public void getCurrentUserReturnOk() throws Exception {
        String getUserResponse = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        User user = mapper.readValue(getUserResponse, User.class);
        UsernamePasswordAuthenticationToken userAuth =
            new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword(),
                                                    user.getRoles().stream()
                                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                                        .collect(Collectors.toList()));
        userAuth.setDetails(user.getId());

        mockMvc.perform(get("/api/users/currentuser").with(authentication(userAuth))).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(user.getName()))
               .andExpect(jsonPath("$.surname").value(user.getSurname()))
               .andExpect(jsonPath("$.address").value(user.getAddress()))
               .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    public void getUserWithUserDetailedViewShouldContainIdAndRoles() throws Exception {
        String getUserResponse = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(JsonViews.UserDetailed.class).readValue(getUserResponse, User.class);

        Assertions.assertNotNull(user.getId(), "User ID is null!");
        Assertions.assertNotNull(user.getRoles(), "User roles are null!");
    }

    @Test
    public void getUserWithUserAuthenticationViewShouldNotContainIdAndRoles() throws Exception {
        String getUserResponse = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                        .andExpect(status().isOk())
                                        .andReturn().getResponse().getContentAsString();

        User user = mapper.readerWithView(JsonViews.UserAuthentication.class).readValue(getUserResponse, User.class);

        Assertions.assertNull(user.getId(), "User ID is not null!");
        Assertions.assertNull(user.getRoles(), "User roles are not null!");
    }

    @Test
    public void addAndgetUsersExpectGreaterOrEqualToNumberOfUsersAdded() throws Exception {
        int counter = 3;
        for (int i = 1; i <= counter; i++) {
            User user = new User("Jan", "Kowalski", "jan.kowalski" + i + "@poczta.pl", "moje_haslo",
                                 Collections.singleton(Role.USER));

            mockMvc.perform(post("/api/users")
                   .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/users").with(authentication(MANAGER))).andDo(print())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(Matchers.greaterThanOrEqualTo(counter))));
    }

    @Test
    public void updateUserSurnameReturnOk() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setSurname("Nowak");

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.surname").value(parsedUser.getSurname()));
    }

    @Test
    public void updateUserWithInvalidIdReturnNotFound() throws Exception {
        User user = new User("Jan", "Kowalski", "testowy.email@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
        user.setId(333L);

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserWithEmptyNameReturnUnprocessableEntity() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setName(null);

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    public void updateUserWithEmptySurnameReturnUnprocessableEntity() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setSurname(null);

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("surname"));
    }

    @Test
    public void updateUserWithInvalidEmailAndPasswordReturnUnprocessableEntity() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setEmail("testowy.email");
        parsedUser.setPassword("hasło");

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors", hasItems(hasEntry("field", "email"), hasEntry("field", "password"))));
    }

    @Test
    public void updateUserWithInvalidAddressReturnUnprocessableEntity() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setAddress(new Address("", "12345", "Miasto"));

        mockMvc.perform(put("/api/users").with(authentication(MANAGER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(2)))
               .andExpect(jsonPath("$.errors",
                                   hasItems(hasEntry("field", "address.street"), hasEntry("field", "address.postCode"))));
    }

    @Test
    public void updateUserWithoutAuthorizationReturnForbidden() throws Exception {
        String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

        User parsedUser = mapper.readValue(getUser, User.class);
        parsedUser.setEmail("testowy.email@poczta.pl");

        mockMvc.perform(put("/api/users").with(authentication(USER))
               .content(mapper.writeValueAsString(parsedUser)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserWithAuthorizationReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L).with(authentication(MANAGER))).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUserWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L).with(authentication(USER))).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L).with(authentication(MANAGER))).andDo(print())
               .andExpect(status().isNotFound());
    }
}