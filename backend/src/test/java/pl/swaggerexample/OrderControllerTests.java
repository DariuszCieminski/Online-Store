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
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.swaggerexample.model.*;

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
@Rollback
public class OrderControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	private static final UserBuilder user = SwaggerTests.user;
	private static final UserBuilder manager = SwaggerTests.manager;
	
	@BeforeAll
	public void init() throws Exception
	{
		User u = new User("Jan", "Kowalski", "jan.kowalski123@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
		mockMvc.perform(post("/api/users").with(user(user.build())).content(mapper.writeValueAsString(u)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		
		for (Product product : Arrays.asList(new Product("Product 1", "Description 1", "/url1", BigDecimal.valueOf(2.99D), 1), new Product("Product 2", "Description 2", "/url2", BigDecimal.valueOf(8.99D), 2)))
		{
			mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
	}
	
	@Test
	public void addOrderReturnOk() throws Exception
	{
		Order order = new Order();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Product> productList = mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
		order.setBuyer(mapper.readValue(getUser, User.class));
		order.setItems(productList.stream().map(product -> new OrderItem(product, 5)).collect(Collectors.toSet()));
		
		mockMvc.perform(post("/api/orders").with(user(user.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addOrderReturnUnprocessableEntity() throws Exception
	{
		Order order = new Order();
		order.setBuyer(new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER)));
		order.setItems(Collections.emptySet());
		
		mockMvc.perform(post("/api/orders").with(user(user.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithNegativeProductQuantityReturnUnprocessableEntity() throws Exception
	{
		Order order = new Order();
		Product product = new Product("Product", "Description", "/url", BigDecimal.valueOf(9.99D), 3);
		String getBuyer = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andReturn().getResponse().getContentAsString();
		order.setBuyer(mapper.readValue(getBuyer, User.class));
		order.setItems(Collections.singleton(new OrderItem(product, -5)));
		
		mockMvc.perform(post("/api/orders").with(user(user.build())).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getOrderByValidIdReturnOk() throws Exception
	{
		Order t = new Order();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Product> productList = mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
		t.setBuyer(mapper.readValue(getUser, User.class));
		t.setItems(productList.stream().map(product -> new OrderItem(product, 5)).collect(Collectors.toSet()));
		MvcResult result = mockMvc.perform(post("/api/orders").with(user(user.build())).content(mapper.writeValueAsString(t)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		t = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(get("/api/orders/{id}", t.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(t.getId()));
	}
	
	@Test
	public void getOrderByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/orders/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteOrderWithAuthorizationReturnNoContent() throws Exception
	{
		Order t = new Order();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Product> productList = mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
		t.setBuyer(mapper.readValue(getUser, User.class));
		t.setItems(productList.stream().map(product -> new OrderItem(product, 5)).collect(Collectors.toSet()));
		MvcResult result = mockMvc.perform(post("/api/orders").with(user(user.build())).content(mapper.writeValueAsString(t)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		t = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(delete("/api/orders/{id}", t.getId()).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteOrderWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteOrderWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}