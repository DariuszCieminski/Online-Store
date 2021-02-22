package pl.swaggerexample;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.OrderStatus;
import pl.swaggerexample.model.enums.PaymentMethod;
import pl.swaggerexample.model.enums.Role;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    public void init() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("ALTER SEQUENCE product_sequence RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER SEQUENCE user_sequence RESTART WITH 1").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();

        List<Product> products = Arrays.asList(
            new Product("Product 1", "Description 1", Collections.singleton("/url1"), BigDecimal.valueOf(2.99D), 1),
            new Product("Product 2", "Description 2", Collections.singleton("/url2"), BigDecimal.valueOf(8.99D), 2));

        productDao.saveAll(products);

        User user = new User("Jan", "Kowalski", "user@test.pl", "moje_haslo", Collections.singleton(Role.USER));
        User manager = new User("Jan", "Kowalski", "manager@test.pl", "moje_haslo", Collections.singleton(Role.MANAGER));

        userDao.save(user);
        userDao.save(manager);
    }

    @AfterAll
    public void cleanup() {
        userDao.deleteAll();
        productDao.deleteAll();
    }

    @WithUserDetails("user@test.pl")
    private Order createOrder() throws Exception {
        Order order = new Order();
        String productListJson = mockMvc.perform(get("/api/products"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<Product> productList =
            mapper.readValue(productListJson, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
        order.setItems(productList.stream().map(product -> new OrderItem(product, 1)).collect(Collectors.toSet()));
        order.setDeliveryAddress(new Address("Testowa 1", "01-234", "Testowo"));
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);

        return order;
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderReturnOk() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithoutProductsReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setItems(Collections.emptySet());

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithProductsWithInvalidIdReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        Product p1 = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
        Product p2 = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
        p1.setId(null);
        p2.setId(999L);
        order.getItems().add(new OrderItem(p1, 1));
        order.getItems().add(new OrderItem(p2, 1));

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithNegativeProductQuantityReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.getItems().iterator().next().setQuantity(-5);

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithTooLargeProductQuantityReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.getItems().iterator().next().setQuantity(999);

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithNoDeliveryAddressReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setDeliveryAddress(null);

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithInvalidDeliveryAddressReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setDeliveryAddress(new Address("Testowa 1", "12345", "Testowo"));

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithNoPaymentMethodReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setPaymentMethod(null);

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderShouldHaveStatusCreated() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithTooLongInformationReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setInformation(String.join("", Collections.nCopies(151, "a")));

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void addOrderWithMaximumInformationLengthReturnCreated() throws Exception {
        Order order = createOrder();
        order.setInformation(String.join("", Collections.nCopies(150, "a")));

        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getAllOrdersReturnOk() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getAllOrdersWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrderByValidIdReturnOk() throws Exception {
        Order order = createOrder();
        String orderJson = mockMvc.perform(post("/api/orders")
                                  .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON))
                                  .andExpect(status().isCreated())
                                  .andReturn().getResponse().getContentAsString();
        order = mapper.readValue(orderJson, Order.class);

        mockMvc.perform(get("/api/orders/{id}", order.getId())).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getOrderWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders/1")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrderByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrdersByUserIdReturnOk() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/buyer/{id}", 2L)).andDo(print())
               .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getOrdersByOtherUserIdReturnForbidden() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        User otherUser = new User("Other", "User", "other@user.com", "other_user", Collections.singleton(Role.USER));
        mockMvc.perform(post("/api/users").with(anonymous())
               .content(mapper.writeValueAsString(otherUser)).contentType(MediaType.APPLICATION_JSON))
               .andDo(print())
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/buyer/2")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrdersByInvalidUserIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/buyer/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrderByIdShouldContainBuyerAndProductId() throws Exception {
        Order order = createOrder();
        String postOrder = mockMvc.perform(post("/api/orders")
                                  .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON))
                                  .andExpect(status().isCreated())
                                  .andReturn().getResponse().getContentAsString();
        order = mapper.readValue(postOrder, Order.class);

        mockMvc.perform(get("/api/orders/{id}", order.getId())).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.buyer").exists())
               .andExpect(jsonPath("$.items[0].id").exists());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void getOrdersByBuyerIdShouldNotContainBuyerAndProductId() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/buyer/2")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].buyer").doesNotExist())
               .andExpect(jsonPath("$[0].items[0].id").doesNotExist());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getOrdersByBuyerIdWithoutPermissionShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders/buyer/2")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getOrdersByCurrentUserShouldReturnOk() throws Exception {
        Order order = createOrder();
        mockMvc.perform(post("/api/orders")
               .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/buyer")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteOrderWithAuthorizationReturnNoContent() throws Exception {
        Order order = createOrder();
        String postOrder = mockMvc.perform(post("/api/orders")
                                  .content(mapper.writeValueAsString(order)).contentType(MediaType.APPLICATION_JSON))
                                  .andExpect(status().isCreated())
                                  .andReturn().getResponse().getContentAsString();
        order = mapper.readValue(postOrder, Order.class);

        mockMvc.perform(delete("/api/orders/{id}", order.getId())).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void deleteOrderWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 1L)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteOrderWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }
}