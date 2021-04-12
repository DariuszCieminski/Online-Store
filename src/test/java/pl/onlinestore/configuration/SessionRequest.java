package pl.onlinestore.configuration;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestComponent
@Profile("session")
public class SessionRequest implements CustomRequest {

    @Override
    public MockHttpServletRequestBuilder builder(HttpMethod httpMethod, String urlTemplate, Object... uriVars) {
        return MockMvcRequestBuilders.request(httpMethod, urlTemplate, uriVars).contentType(MediaType.APPLICATION_JSON)
                                     .with(SecurityMockMvcRequestPostProcessors.csrf());
    }
}