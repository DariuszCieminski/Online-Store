package pl.swaggerexample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.Transaction;
import pl.swaggerexample.model.User;
import pl.swaggerexample.service.TransactionService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.core.userdetails.User.UserBuilder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureMockMvc
public class TransactionControllerTests
{
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private TransactionService transactionService;
	
	private static UserBuilder user = SwaggerTests.user;
	private static UserBuilder manager = SwaggerTests.manager;
	
	@Test
	public void addTransactionExpectSuccess() throws Exception
	{
		Transaction transaction = new Transaction();
		transaction.setBuyer(new pl.swaggerexample.model.User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo"));
		transaction.getBuyer().setId(123L);
		transaction.setProducts(Arrays.asList(new Product("Product 1", "Description 1", "url1", 2.99D), new Product("Product 2", "Description 2", "url2", 8.99D)));
		
		
		mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(transaction)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isCreated());
	}
	
	@Test
	public void addTransactionExpectUnprocessableEntity() throws Exception
	{
		Transaction transaction = new Transaction();
		transaction.setBuyer(new pl.swaggerexample.model.User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo"));
		transaction.getBuyer().setId(123L);
		transaction.setProducts(Collections.emptyList());
		
		mockMvc.perform(post("/api/transactions").with(user(user.build())).content(mapper.writeValueAsString(transaction)).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isUnprocessableEntity());
	}
	
	@Test
	public void getTransactionByValidId() throws Exception
	{
		Transaction t = new Transaction();
		t.setBuyer(new pl.swaggerexample.model.User("Jan", "Kowalski", "jan.kowalski@poczta.pl", "moje_haslo"));
		t.getBuyer().setId(123L);
		t.setProducts(Collections.singletonList(new Product("", "", "", 1D)));
		t = transactionService.addTransaction(t);
		mockMvc.perform(get("/api/transactions/{id}", t.getId()).with(user(user.build()))).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(t.getId()));
	}
	
	@Test
	public void getTransactionByInvalidId() throws Exception
	{
		mockMvc.perform(get("/api/transactions/{id}", 999L).with(user(user.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void deleteTransactionWithAuthorizationExpectSuccess() throws Exception
	{
		Transaction t = new Transaction();
		t.setBuyer(new User("", "", "", ""));
		t.getBuyer().setId(123L);
		t.setProducts(Collections.singletonList(new Product("", "", "", 1D)));
		t = transactionService.addTransaction(t);
		mockMvc.perform(delete("/api/transactions/{id}", t.getId()).with(user(manager.build()))).andDo(print()).andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteTransactionWithoutAuthorizationExpectFail() throws Exception
	{
		mockMvc.perform(delete("/api/transactions/{id}", 1L).with(user(user.build()))).andDo(print()).andExpect(status().isForbidden());
	}
	
	@Test
	public void deleteTransactionWithInvalidId() throws Exception
	{
		mockMvc.perform(delete("/api/transactions/{id}", 999L).with(user(manager.build()))).andDo(print()).andExpect(status().isNotFound());
	}
	
	@Test
	public void testCalculateTransactionCostExpectTrue()
	{
		Transaction transaction = new Transaction();
		List<Product> products = Arrays.asList(new Product("", "", "", 2.99D),
				new Product("", "", "", 6.50D),
				new Product("", "", "", 15.20D),
				new Product("", "", "", 4.40D),
				new Product("", "", "", 25.49D));
		
		transaction.setProducts(products);
		Double cost = 0.00D;
		for (Product product : transaction.getProducts())
		{
			cost += product.getPrice();
		}
		
		Assert.isTrue(cost.equals(transaction.getCost()), "Transaction costs are not equal!");
	}
}