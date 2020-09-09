package pl.swaggerexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableSwagger2WebMvc
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
				.apiInfo(getApiInfo());
	}
	
	private ApiInfo getApiInfo()
	{
		return new ApiInfoBuilder()
				.title("Swagger Example Application")
				.description("Simple REST Application with Swagger")
				.version("1.0")
				.build();
	}
}