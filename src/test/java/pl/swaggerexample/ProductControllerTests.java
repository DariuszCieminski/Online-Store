package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.test.web.servlet.MockMvc;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.service.ProductService;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureMockMvc
public class ProductControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private ProductService productService;
	
	private static UserBuilder user = SwaggerTests.user;
	private static UserBuilder manager = SwaggerTests.manager;
	
	@Autowired
	public ProductControllerTests(ProductService productService)
	{
		this.productService = productService;
		productService.addProduct(new Product("Milk", "A carton of milk.", "milkimageurl", 4.99D));
		productService.addProduct(new Product("Bread", "A single loaf of bread.", "breadimageurl", 2.79D));
		productService.addProduct(new Product("Ham", "250g of ham.", "hamimageurl", 10.50D));
		productService.addProduct(new Product("Coffee", "A packet of coffee.", "coffeeimageurl", 15.00D));
		productService.addProduct(new Product("Butter", "500g of butter.", "butterimageurl", 7.29D));
	}
	
	@Test
	public void createProductExpectSuccess() throws Exception
	{
		Product product = new Product("Product 1", "Simple description of Product 1", "https://picsum.photos/200", 11.99D);
		mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(product.getName())).andExpect(jsonPath("$.price").value(product.getPrice()));
	}
	
	@Test
	public void createProductWithoutNameExpectUnprocessableEntity() throws Exception
	{
		Product product = new Product("", "Simple description of Product 1", "https://picsum.photos/200", 11.99D);
		mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getProductByValidId() throws Exception
	{
		Product product = productService.addProduct(new Product("Product", "Simple description of product", "https://picsum.photos/200", 11.99D));
		mockMvc.perform(get("/api/products/{id}", product.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(product.getId()));
	}
	
	@Test
	public void getProductByInvalidId() throws Exception
	{
		mockMvc.perform(get("/api/products/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@ParameterizedTest
	@MethodSource("getRequestParams")
	public void getProductsFilteredByCustomPredicatesExpectSuccess(SimpleImmutableEntry<String, String> param) throws Exception
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
	public void getProductsFilteredByCustomPredicatesExpectBadRequest400() throws Exception
	{
		mockMvc.perform(get("/api/products").queryParam("priceEqualTo", "cena").with(user(user.build()))).andDo(print()).andExpect(status().isBadRequest());
	}
	
	@Test
	public void updateProductExpectSuccess() throws Exception
	{
		Product p = productService.addProduct(new Product("Product", "Description", "url", 3.99D));
		p.setName("Updated product");
		p.setDescription("Updated description");
		p.setPrice(5.99D);
		mockMvc.perform(put("/api/products").with(user(manager.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.name").value(p.getName())).andExpect(jsonPath("$.description").value(p.getDescription())).andExpect(jsonPath("$.price").value(p.getPrice()));
	}
	
	@Test
	public void updateProductWithInvalidIdExpectNotFound() throws Exception
	{
		Product p = new Product("Product", "Description", "url", 3.99D);
		p.setId(333L);
		mockMvc.perform(put("/api/products").with(user(user.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void updateProductWithInvalidPriceExpectUnprocessableEntity() throws Exception
	{
		Product p = productService.addProduct(new Product("Product", "Description", "url", 3.99D));
		p.setPrice(-8.99D);
		mockMvc.perform(put("/api/products").with(user(user.build())).content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void deleteProductWithAuthorizationExpectSuccess() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 1L).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteProductWithoutAuthorizationExpectFail() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteProductWithInvalidId() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}