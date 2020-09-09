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
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.Role;
import pl.swaggerexample.model.Transaction;
import pl.swaggerexample.model.User;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class TransactionControllerTests
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
		
		for (Product product : Arrays.asList(new Product("Product 1", "Description 1", "/url1", BigDecimal.valueOf(2.99D)), new Product("Product 2", "Description 2", "/url2", BigDecimal.valueOf(8.99D))))
		{
			mockMvc.perform(post("/api/products").with(user(user.build())).content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
		}
	}
	
	@Test
	public void addTransactionReturnOk() throws Exception
	{
		Transaction transaction = new Transaction();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		transaction.setBuyer(mapper.readValue(getUser, User.class));
		transaction.setProducts(mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class)));
		
		mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(transaction)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addTransactionReturnUnprocessableEntity() throws Exception
	{
		Transaction transaction = new Transaction();
		transaction.setBuyer(new User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo", Collections.singleton(Role.USER)));
		transaction.setProducts(Collections.emptyList());
		
		mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(transaction)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getTransactionByValidIdReturnOk() throws Exception
	{
		Transaction t = new Transaction();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		t.setBuyer(mapper.readValue(getUser, User.class));
		t.setProducts(mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class)));
		MvcResult result = mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(t)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		t = mapper.readValue(result.getResponse().getContentAsString(), Transaction.class);
		
		mockMvc.perform(get("/api/transactions/{id}", t.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(t.getId()));
	}
	
	@Test
	public void getTransactionByInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(get("/api/transactions/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteTransactionWithAuthorizationReturnNoContent() throws Exception
	{
		Transaction t = new Transaction();
		String getUser = mockMvc.perform(get("/api/users/1").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		String getProducts = mockMvc.perform(get("/api/products").with(user(user.build()))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		t.setBuyer(mapper.readValue(getUser, User.class));
		t.setProducts(mapper.readValue(getProducts, mapper.getTypeFactory().constructCollectionType(List.class, Product.class)));
		MvcResult result = mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(t)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andReturn();
		t = mapper.readValue(result.getResponse().getContentAsString(), Transaction.class);
		
		mockMvc.perform(delete("/api/transactions/{id}", t.getId()).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteTransactionWithoutAuthorizationReturnForbidden() throws Exception
	{
		mockMvc.perform(delete("/api/transactions/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteTransactionWithInvalidIdReturnNotFound() throws Exception
	{
		mockMvc.perform(delete("/api/transactions/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
}