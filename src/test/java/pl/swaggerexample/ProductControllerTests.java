package pl.swaggerexample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.swaggerexample.model.Product;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class, properties = "spring.jpa.properties.javax.persistence.validation.mode=ddl")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Rollback
public class ProductControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static UserBuilder user = SwaggerTests.user;
	private static UserBuilder manager = SwaggerTests.manager;
	
	@BeforeAll
	public void init() throws Exception
	{
		List<Product> products = Arrays.asList(
				new Product("Milk", "A carton of milk.", "/milkimageurl", BigDecimal.valueOf(4.99D)),
				new Product("Bread", "A single loaf of bread.", "/breadimageurl", BigDecimal.valueOf(2.79D)),
				new Product("Ham", "250g of ham.", "/hamimageurl", BigDecimal.valueOf(10.50D)),
				new Product("Coffee", "A packet of coffee.", "/coffeeimageurl", BigDecimal.valueOf(15.00D)),
				new Product("Butter", "500g of butter.", "/butterimageurl", BigDecimal.valueOf(7.29D))
		);
		
		for (Product product : products)
		{
			mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
	}
	
	@Test
	public void createProductReturnOk() throws Exception
	{
		Product product = new Product("Product 1", "Simple description of Product 1", "https://picsum.photos/200", BigDecimal.valueOf(11.99D));
		mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(product.getName())).andExpect(jsonPath("$.price").value(product.getPrice()));
	}
	
	@Test
	public void createProductWithoutNameReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("", "Simple description of Product 1", "https://picsum.photos/200", BigDecimal.valueOf(11.99D));
		mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductWithInvalidPriceReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("Product", "Description", "https://picsum.photos/200", BigDecimal.valueOf(-8.99D));
		mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getProductByValidIdReturnOk() throws Exception
	{
		Product product = new Product("Product", "Simple description of product", "https://picsum.photos/200", BigDecimal.valueOf(11.99D));
		MvcResult result = mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		product = mapper.readValue(result.getResponse().getContentAsString(), Product.class);
		
		mockMvc.perform(get("/api/products/{id}", product.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(product.getId()));
	}
	
	@Test
	public void getProductByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/products/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@ParameterizedTest
	@MethodSource("getRequestParams")
	public void getProductsFilteredByCustomPredicatesReturnOk(SimpleImmutableEntry<String, String> param) throws Exception
	{
		mockMvc.perform(get("/api/products").queryParam(param.getKey(), param.getValue()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))));
	}
	
	private static List<SimpleImmutableEntry<String, String>> getRequestParams()
	{
		return Arrays.asList(new SimpleImmutableEntry<>("nameContains", "Milk"),
				new SimpleImmutableEntry<>("descContains", "packet"),
				new SimpleImmutableEntry<>("priceGreaterThan", "6.00"),
				new SimpleImmutableEntry<>("priceLessThan", "10.50"),
				new SimpleImmutableEntry<>("priceEqualTo", "7.29"));
	}
	
	@Test
	public void getProductsFilteredByCustomPredicatesReturnBadRequest400() throws Exception
	{
		mockMvc.perform(get("/api/products").queryParam("priceEqualTo", "price").with(user(user.build()))).andDo(print()).andExpect(status().isBadRequest());
	}
	
	@Test
	public void updateProductReturnOk() throws Exception
	{
		MvcResult result = mockMvc.perform(get("/api/products").with(user(user.build()))).andReturn();
		JsonNode jsonNode = mapper.readTree(result.getResponse().getContentAsString()).get(0);
		Product p = mapper.readValue(jsonNode.toString().getBytes(), Product.class);
		p.setName("Updated product");
		p.setDescription("Updated description");
		p.setPrice(BigDecimal.valueOf(5.99D));
		
		mockMvc.perform(put("/api/products").with(user(manager.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.name").value(p.getName())).andExpect(jsonPath("$.description").value(p.getDescription())).andExpect(jsonPath("$.price").value(p.getPrice()));
	}
	
	@Test
	public void updateProductWithInvalidIdReturnNotFound() throws Exception
	{
		Product p = new Product("Product", "Description", "https://picsum.photos/200", BigDecimal.valueOf(3.99D));
		p.setId(333L);
		
		mockMvc.perform(put("/api/products").with(user(user.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteProductWithAuthorizationReturnNoContent() throws Exception
	{
		Product p = new Product("Product", "Description", "https://picsum.photos/200", BigDecimal.valueOf(3.99D));
		MvcResult result = mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		p = mapper.readValue(result.getResponse().getContentAsString(), Product.class);
		mockMvc.perform(delete("/api/products/{id}", p.getId()).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteProductWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteProductWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}