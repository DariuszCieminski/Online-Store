package pl.swaggerexample;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.model.Product;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class ProductControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static final UserBuilder USER = SwaggerTests.USER;
	private static final UserBuilder MANAGER = SwaggerTests.MANAGER;
	
	@BeforeAll
	public void init() throws Exception
	{
		List<Product> products = Arrays.asList(
				new Product("Milk", "A carton of milk.", Collections.singleton("/milkimageurl"), BigDecimal.valueOf(4.99D), 20),
				new Product("Bread", "A single loaf of bread.", Collections.singleton("/breadimageurl"), BigDecimal.valueOf(2.79D), 32),
				new Product("Ham", "250g of ham.", Collections.singleton("/hamimageurl"), BigDecimal.valueOf(10.50D), 16),
				new Product("Coffee", "A packet of coffee.", Collections.singleton("/coffeeimageurl"), BigDecimal.valueOf(15.00D), 10),
				new Product("Butter", "500g of butter.", Collections.singleton("/butterimageurl"), BigDecimal.valueOf(7.29D), 8)
		);
		
		for (Product product : products)
		{
			mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
					.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isCreated());
		}
	}
	
	@Test
	public void createProductReturnOk() throws Exception
	{
		Product product = new Product("Product 1", "Simple description of Product 1", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(11.99D), 3);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(product.getName()))
				.andExpect(jsonPath("$.description").value(product.getDescription()))
				.andExpect(jsonPath("$.images").isArray())
				.andExpect(jsonPath("$.images", contains(product.getImages().toArray())))
				.andExpect(jsonPath("$.price").value(product.getPrice()))
				.andExpect(jsonPath("$.quantity").value(product.getQuantity()));
	}
	
	@Test
	public void createProductWithoutNameReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("", "Simple description of Product 1", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(11.99D), 1);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductWithInvalidImageUrlReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("Product", "Simple description of Product 1", Collections.singleton("image_url"), BigDecimal.valueOf(11.99D), 1);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductWithNegativePriceReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(-8.99D), 1);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductWithPriceWithThreeDecimalPlacesReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(8.999D), 1);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductWithNegativeQuantityReturnUnprocessableEntity() throws Exception
	{
		Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(-8.99D), -5);
		mockMvc.perform(post("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void createProductByNonManagerReturnForbidden() throws Exception
	{
		Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(11.99D), 1);
		mockMvc.perform(post("/api/products").with(user(USER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void getProductByValidIdReturnOk() throws Exception
	{
		Long id = 1L;
		mockMvc.perform(get("/api/products/{id}", id).with(user(USER.build()))).andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(id));
	}
	
	@Test
	public void getProductByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/products/{id}", 999L).with(user(USER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@ParameterizedTest
	@MethodSource("getRequestParams")
	public void getProductsFilteredByCustomPredicatesReturnOk(SimpleImmutableEntry<String, String> param) throws Exception
	{
		mockMvc.perform(get("/api/products").queryParam(param.getKey(), param.getValue()).with(user(USER.build()))).andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))));
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
	public void getProductsFilteredByCustomPredicateWithWrongValueReturnBadRequest() throws Exception
	{
		mockMvc.perform(get("/api/products").queryParam("priceEqualTo", "price").with(user(USER.build())))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void updateProductReturnOk() throws Exception
	{
		MvcResult getProduct = mockMvc.perform(get("/api/products/1").with(user(USER.build()))).andReturn();
		Product product = mapper.readValue(getProduct.getResponse().getContentAsString(), Product.class);
		product.setName("Updated product");
		product.setDescription("Updated description");
		product.setPrice(BigDecimal.valueOf(5.99D));
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(product.getName()))
				.andExpect(jsonPath("$.description").value(product.getDescription()))
				.andExpect(jsonPath("$.price").value(product.getPrice()));
	}
	
	@Test
	public void updateProductWithInvalidIdReturnNotFound() throws Exception
	{
		Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(3.99D), 1);
		product.setId(333L);
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateProductWithEmptyNameReturnUnprocessableEntity() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(MANAGER.build())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Product product = mapper.readValue(getProduct, Product.class);
		product.setName(null);
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("name"));
	}
	
	@Test
	public void updateProductWithInvalidImageUrlReturnUnprocessableEntity() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(MANAGER.build())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Product product = mapper.readValue(getProduct, Product.class);
		product.getImages().add("image_url");
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("images[]"));
	}
	
	@Test
	public void updateProductWithNegativePriceReturnUnprocessableEntity() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(MANAGER.build())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Product product = mapper.readValue(getProduct, Product.class);
		product.setPrice(BigDecimal.valueOf(-8.99D));
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("price"));
	}
	
	@Test
	public void updateProductWithPriceWithThreeDecimalPlacesReturnUnprocessableEntity() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(MANAGER.build())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Product product = mapper.readValue(getProduct, Product.class);
		product.setPrice(BigDecimal.valueOf(5.999D));
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("price"));
	}
	
	@Test
	public void updateProductWithNegativeQuantityReturnUnprocessableEntity() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(MANAGER.build())))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		Product product = mapper.readValue(getProduct, Product.class);
		product.setQuantity(-2);
		
		mockMvc.perform(put("/api/products").with(user(MANAGER.build()))
				.content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.errors").isArray())
				.andExpect(jsonPath("$.errors", hasSize(1)))
				.andExpect(jsonPath("$.errors[0].field").value("quantity"));
	}
	
	@Test
	public void updateProductWithoutAuthorizationReturnForbidden() throws Exception
	{
		String getProduct = mockMvc.perform(get("/api/products/1").with(user(USER.build())))
				.andReturn().getResponse().getContentAsString();
		Product p = mapper.readValue(getProduct, Product.class);
		p.setName("Updated product name");
		
		mockMvc.perform(put("/api/products").with(user(USER.build()))
				.content(mapper.writeValueAsString(p)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteProductWithAuthorizationReturnNoContent() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 1L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteProductWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 1L).with(user(USER.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteProductWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/products/{id}", 999L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}