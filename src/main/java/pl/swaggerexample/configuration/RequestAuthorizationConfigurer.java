package pl.swaggerexample.configuration;

import static pl.swaggerexample.security.jwt.SwaggerAuthenticationFilter.SWAGGER_PATH_MATCHERS;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import pl.swaggerexample.model.enums.Role;

public class RequestAuthorizationConfigurer implements
    Customizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> {

    @Override
    public void customize(
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry) {
        expressionInterceptUrlRegistry
            .antMatchers(HttpMethod.DELETE).hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.GET, SWAGGER_PATH_MATCHERS.toArray(new String[0])).hasRole(Role.DEVELOPER.name())
            .antMatchers(HttpMethod.GET, "/api/orders/buyer").authenticated()
            .antMatchers(HttpMethod.GET, "/api/users/**", "/api/orders/**").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.POST, "/api/products").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.PUT, "/api/products", "/api/users").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.POST, "/api/users").permitAll()
            .antMatchers(HttpMethod.POST, "/login").anonymous()
            .anyRequest().authenticated();
    }
}