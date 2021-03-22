package pl.swaggerexample.configuration;

import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public interface CustomRequest {

    MockHttpServletRequestBuilder builder(HttpMethod httpMethod, String urlTemplate, Object... uriVars);
}