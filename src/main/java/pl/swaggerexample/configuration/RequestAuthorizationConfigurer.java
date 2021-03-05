package pl.swaggerexample.configuration;

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
            .antMatchers(HttpMethod.GET, "/api/orders/buyer").authenticated()
            .antMatchers(HttpMethod.GET, "/api/users/**", "/api/orders/**").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.POST, "/api/products").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.PUT, "/api/products", "/api/users").hasRole(Role.MANAGER.name())
            .antMatchers(HttpMethod.POST, "/api/users").permitAll()
            .anyRequest().authenticated();
    }
}