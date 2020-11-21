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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.model.*;
import pl.swaggerexample.model.enums.PaymentMethod;
import pl.swaggerexample.model.enums.Role;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class)
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
	
	private static final Authentication USER = new UsernamePasswordAuthenticationToken(SwaggerTests.USER.build().getUsername(), SwaggerTests.USER.build().getPassword(), SwaggerTests.USER.build().getAuthorities());
	private static final Authentication MANAGER = new UsernamePasswordAuthenticationToken(SwaggerTests.MANAGER.build().getUsername(), SwaggerTests.MANAGER.build().getPassword(), SwaggerTests.MANAGER.build().getAuthorities());
	
	@BeforeAll
	public void init() throws Exception
	{
		User user = new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER));
		mockMvc.perform(post("/api/users").content(mapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		
		for (Product product : Arrays.asList(new Product("Product 1", "Description 1", Collections.singleton("/url1"), BigDecimal.valueOf(2.99D), 1), new Product("Product 2", "Description 2", Collections.singleton("/url2"), BigDecimal.valueOf(8.99D), 2)))
		{
			mockMvc.perform(post("/api/products").with(authentication(MANAGER)).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
		
		((UsernamePasswordAuthenticationToken) USER).setDetails(1L);
		((UsernamePasswordAuthenticationToken) MANAGER).setDetails(2L);
	}
	
	private Order createOrder() throws Exception
	{
		Order order = new Order();
		String getUser = mockMvc.perform(get("/api/users/1").with(authentication(MANAGER))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(authentication(USER))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		List<Product> productList = mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
		order.setBuyer(mapper.readValue(getUser, User.class));
		order.setItems(productList.stream().map(product -> new OrderItem(product, 1)).collect(Collectors.toSet()));
		order.setDeliveryAddress(new Address("Testowa 1", "01-234", "Testowo"));
		order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
		
		return order;
	}
	
	@Test
	public void addOrderReturnOk() throws Exception
	{
		Order order = createOrder();
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addOrderWithoutProductsReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setItems(Collections.emptySet());
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithoutBuyerReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setBuyer(null);
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderAsAnotherBuyerReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		
		mockMvc.perform(post("/api/orders").with(authentication(MANAGER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithNegativeProductQuantityReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		Product product = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
		order.getItems().add(new OrderItem(product, -5));
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithTooLargeProductQuantityReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		Product product = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 5);
		order.getItems().add(new OrderItem(product, 10));
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithNoDeliveryAddressReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setDeliveryAddress(null);
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithInvalidDeliveryAddressReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setDeliveryAddress(new Address("Testowa 1", "12345", "Testowo"));
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithNoPaymentMethodReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setPaymentMethod(null);
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void addOrderWithTooLongInformationReturnUnprocessableEntity() throws Exception
	{
		Order order = createOrder();
		order.setInformation(String.join("", Collections.nCopies(151, "a")));
		
		mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getOrderByValidIdReturnOk() throws Exception
	{
		Order order = createOrder();
		MvcResult result = mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		order = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(get("/api/orders/{id}", order.getId()).with(authentication(MANAGER))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(order.getId()));
	}
	
	@Test
	public void getOrderByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/orders/{id}", 999L).with(authentication(MANAGER))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void getOrdersByUserIdReturnOk() throws Exception
	{
		
	}
	
	@Test
	public void getOrdersByOtherUserIdReturnForbidden() throws Exception
	{
	
	}
	
	@Test
	public void deleteOrderWithAuthorizationReturnNoContent() throws Exception
	{
		Order order = createOrder();
		MvcResult result = mockMvc.perform(post("/api/orders").with(authentication(USER)).content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		order = mapper.readValue(result.getResponse().getContentAsString(), Order.class);
		
		mockMvc.perform(delete("/api/orders/{id}", order.getId()).with(authentication(MANAGER))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteOrderWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 1L).with(authentication(USER))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteOrderWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/orders/{id}", 999L).with(authentication(MANAGER))).andDo(print()).andExpect(status().isNotFound());
	}
}