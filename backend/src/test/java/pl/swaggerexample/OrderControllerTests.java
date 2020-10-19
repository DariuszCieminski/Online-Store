package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.model.*;
import pl.swaggerexample.security.JwtAuthorizationFilter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.core.userdetails.User.UserBuilder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class, properties = "spring.jpa.properties.javax.persistence.validation.mode=ddl")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DirtiesContext
public class OrderControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static final UserBuilder USER = SwaggerTests.USER;
	private static final UserBuilder MANAGER = SwaggerTests.MANAGER;
	
	private Order createOrder() throws Exception
	{
		Order order = new Order();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(MANAGER.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(USER.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Product> productList = mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
		order.setBuyer(mapper.readValue(getUser, User.class));
		order.setItems(productList.stream().map(product -> new OrderItem(product, 5)).collect(Collectors.toSet()));
		
		return order;
	}
	
	private String getAccessToken() throws Exception
	{
		String loginContent = String.format(JwtTests.LOGIN_TEMPLATE, "jan.kowalski@poczta.pl", "moje_haslo");
		String loginResponse = mockMvc.perform(post("/login").content(loginContent)).andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		
		return mapper.readTree(loginResponse).get("access_token").textValue();
	}
	
	@BeforeAll
	public void init() throws Exception
	{
		User user = new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
		mockMvc.perform(post("/api/users").with(user(USER.build())).content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		
		for (Product product : Arrays.asList(new Product("Product 1", "Description 1", Collections.singleton("/url1"), BigDecimal.valueOf(2.99D), 1), new Product("Product 2", "Description 2", Collections.singleton("/url2"), BigDecimal.valueOf(8.99D), 2)))
		{
			mockMvc.perform(post("/api/products").with(user(MANAGER.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
	}
	
	@Test
	public void addOrderReturnOk() throws Exception
	{
		Order order = createOrder();
		String accessToken = getAccessToken();
		
		mockMvc.perform(post("/api/orders").header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX + accessToken).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addOrderReturnUnprocessableEntity() throws Exception
	{
		Order order = new Order();
		order.setBuyer(new User("Jan", "Kowalski", "jan.kowalski123@poczta.pl", "moje_haslo", Collections.singleton(Role.USER)));
		order.setItems(Collections.emptySet());
		
		mockMvc.perform(post("/api/orders").with(user(USER.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithNegativeProductQuantityReturnUnprocessableEntity() throws Exception
	{
		Order order = new Order();
		Product product = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
		String getBuyer = mockMvc.perform(get("/api/users/1").with(user(USER.build()))).andReturn().getResponse().getContentAsString();
		order.setBuyer(mapper.readValue(getBuyer, User.class));
		order.setItems(Collections.singleton(new OrderItem(product, -5)));
		
		mockMvc.perform(post("/api/orders").with(user(USER.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getOrderByValidIdReturnOk() throws Exception
	{
		Order order = createOrder();
		String accessToken = getAccessToken();
		MvcResult result = mockMvc.perform(post("/api/orders").header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX + accessToken).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		order = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(get("/api/orders/{id}", order.getId()).header(JwtAuthorizationFilter.AUTH_HEADER, JwtAuthorizationFilter.AUTH_PREFIX + accessToken)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(order.getId()));
	}
	
	@Test
	public void getOrderByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/orders/{id}", 999L).with(user(USER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void getOrdersByUserIdReturnOk() throws Exception
	{
		
	}
	
	@Test
	public void getOrdersWithoutPermissionReturnUnauthorized() throws Exception
	{
	
	}
	
	@Test
	public void deleteOrderWithAuthorizationReturnNoContent() throws Exception
	{
		Order order = createOrder();
		MvcResult result = mockMvc.perform(post("/api/orders").with(user(USER.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		order = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(delete("/api/orders/{id}", order.getId()).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteOrderWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 1L).with(user(USER.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteOrderWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 999L).with(user(MANAGER.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}