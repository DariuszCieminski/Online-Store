package pl.swaggerexample;

import static org.springframework.security.core.userdetails.User.builder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.security.JwtManager;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureMockMvc
public class SwaggerTests {

    private static final UserBuilder USER = builder().username("user").password("user").roles(Role.USER.name());
    private static final UserBuilder DEVELOPER = builder().username("dev").password("dev").roles(Role.DEVELOPER.name());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtManager jwt;

    @Test
    public void openSwaggerWithCookieShouldReturnOk() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(DEVELOPER.build().getUsername(),
                                                                      DEVELOPER.build().getPassword(),
                                                                      DEVELOPER.build().getAuthorities());

        Cookie swaggerCookie = new Cookie("swagger_id", jwt.generateSwaggerToken(auth));

        mockMvc.perform(get("/swagger-ui/index.html").with(user(DEVELOPER.build()))
               .cookie(swaggerCookie))
               .andExpect(status().isOk());

        mockMvc.perform(get("/v2/api-docs").with(user(DEVELOPER.build()))
               .cookie(swaggerCookie))
               .andExpect(status().isOk());
    }

    @Test
    public void openSwaggerWithoutCookieShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").with(user(DEVELOPER.build())))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));

        mockMvc.perform(get("/v2/api-docs").with(user(DEVELOPER.build())))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));
    }

    @Test
    public void openSwaggerWithInvalidCookieShouldReturnUnauthorized() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(DEVELOPER.build().getUsername(),
                                                                      DEVELOPER.build().getPassword(),
                                                                      DEVELOPER.build().getAuthorities());

        Cookie invalidSwaggerCookie = new Cookie("swagger_id", jwt.generateAccessToken(auth));

        mockMvc.perform(get("/swagger-ui/index.html").with(user(DEVELOPER.build()))
               .cookie(invalidSwaggerCookie))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));

        mockMvc.perform(get("/v2/api-docs").with(user(DEVELOPER.build()))
               .cookie(invalidSwaggerCookie))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));
    }

    @Test
    public void openSwaggerAsRegularUserShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").with(user(USER.build())))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));

        mockMvc.perform(get("/v2/api-docs").with(user(USER.build())))
               .andExpect(status().isUnauthorized())
               .andExpect(content().string("UNAUTHORIZED"));
    }
}