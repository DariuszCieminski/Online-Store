package pl.swaggerexample;

import io.swagger.models.auth.In;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@SpringBootApplication
public class SwaggerExampleApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(SwaggerExampleApplication.class, args);
	}
	
	@Bean
	public Docket docket()
	{
		return new Docket(DocumentationType.SWAGGER_2)
				.useDefaultResponseMessages(false)
				.select().paths(PathSelectors.ant("/api/**"))
				.build()
				.apiInfo(getApiInfo())
				.securitySchemes(Collections.singletonList(getApiKey()))
				.securityContexts(Collections.singletonList(getSecurityContext()));
	}
	
	private ApiInfo getApiInfo()
	{
		return new ApiInfoBuilder()
				.title("Swagger Example Application")
				.description("Simple REST Application with Swagger")
				.version("1.0")
				.build();
	}
	
	private ApiKey getApiKey()
	{
		return new ApiKey("JWT", HttpHeaders.AUTHORIZATION, In.HEADER.toValue());
	}
	
	private SecurityContext getSecurityContext()
	{
		SecurityReference securityReference = SecurityReference.builder().reference("JWT").scopes(new AuthorizationScope[0]).build();
		
		return SecurityContext.builder().securityReferences(Collections.singletonList(securityReference)).build();
	}
}