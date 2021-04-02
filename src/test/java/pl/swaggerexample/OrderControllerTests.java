package pl.swaggerexample;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.configuration.CustomRequest;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.model.Address;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.OrderStatus;
import pl.swaggerexample.model.enums.PaymentMethod;
import pl.swaggerexample.model.enums.Role;
import pl.swaggerexample.service.OrderService;
import pl.swaggerexample.service.ProductService;

@SpringBootTest
@ComponentScan
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private OrderService orderService;

    @SpyBean
    private ProductService productService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CustomRequest request;

    List<Order> orderList;

    @BeforeEach
    void initOrder() {
        Mockito.when(orderService.getByBuyerId(1L)).thenReturn(orderList);
        Mockito.when(orderService.getAll()).thenReturn(orderList);
        Mockito.doReturn(orderList).when(orderService).getByCurrentUser();
    }

    @BeforeAll
    void initUsersAndProducts() {
        List<Product> products = Arrays.asList(
            new Product("Product 1", "Description 1", Collections.singleton("/url1"), BigDecimal.valueOf(2.99D), 1),
            new Product("Product 2", "Description 2", Collections.singleton("/url2"), BigDecimal.valueOf(8.99D), 2));

        User user = new User("Jan", "Kowalski", "user@test.pl", "moje_haslo", Collections.singleton(Role.USER));
        User manager = new User("Jan", "Kowalski", "manager@test.pl", "moje_haslo", Collections.singleton(Role.MANAGER));

        userDao.save(user);
        userDao.save(manager);
        products.forEach(productService::add);
        orderList = Collections.singletonList(createOrder());
    }

    @AfterAll
    void cleanup() {
        userDao.deleteAll();
        productService.getAll().forEach(product -> productService.delete(product.getId()));
    }

    private Order createOrder() {
        Order order = new Order();
        List<Product> products = productService.getAll();
        order.setItems(products.stream().map(product -> new OrderItem(product, 1)).collect(Collectors.toSet()));
        order.setDeliveryAddress(new Address("Testowa 1", "01-234", "Testowo"));
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setStatus(OrderStatus.CREATED);
        order.setTime(OffsetDateTime.now());

        return order;
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderReturnOk() throws Exception {
        Order order = createOrder();
        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithoutProductsReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setItems(Collections.emptySet());

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithProductsWithInvalidIdReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        Product p1 = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
        Product p2 = new Product("Product", "Description", Collections.singleton("/url"), BigDecimal.valueOf(9.99D), 3);
        p1.setId(null);
        p2.setId(999L);
        order.getItems().add(new OrderItem(p1, 1));
        order.getItems().add(new OrderItem(p2, 1));

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithNegativeProductQuantityReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.getItems().iterator().next().setQuantity(-5);

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithTooLargeProductQuantityReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.getItems().iterator().next().setQuantity(999);

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithNoDeliveryAddressReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setDeliveryAddress(null);

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithInvalidDeliveryAddressReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setDeliveryAddress(new Address("Testowa 1", "12345", "Testowo"));

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithNoPaymentMethodReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setPaymentMethod(null);

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderShouldHaveOrderStatusCreated() throws Exception {
        Order order = createOrder();
        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()));
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithTooLongInformationReturnUnprocessableEntity() throws Exception {
        Order order = createOrder();
        order.setInformation(String.join("", Collections.nCopies(151, "a")));

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void addOrderWithMaximumInformationLengthReturnCreated() throws Exception {
        Order order = createOrder();
        order.setInformation(String.join("", Collections.nCopies(150, "a")));

        mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
               .content(mapper.writeValueAsString(order))).andDo(print())
               .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getAllOrdersReturnOk() throws Exception {
        mockMvc.perform(get("/api/orders")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("user@test.pl")
    void getAllOrdersWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getOrderByValidIdReturnOk() throws Exception {
        Order order = createOrder();
        String orderJson = mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
                                  .content(mapper.writeValueAsString(order)))
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
    void getOrderWithoutPermissionReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders/1")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getOrderByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getOrdersByUserIdReturnOk() throws Exception {
        mockMvc.perform(get("/api/orders/buyer/1")).andDo(print())
               .andExpect(status().isOk());
    }

    @Test
    void getOrdersByOtherUserIdReturnForbidden() throws Exception {
        User otherUser = new User("Other", "User", "other@user.com", "other_user", Collections.singleton(Role.USER));
        mockMvc.perform(request.builder(HttpMethod.POST,"/api/users").with(anonymous())
               .content(mapper.writeValueAsString(otherUser)))
               .andDo(print())
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders/buyer/1").with(user("other@user.com"))).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getOrdersByInvalidUserIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/buyer/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void getOrderByIdShouldContainBuyerAndProductId() throws Exception {
        Order order = createOrder();
        String postOrder = mockMvc.perform(request.builder(HttpMethod.POST,"/api/orders")
                                  .content(mapper.writeValueAsString(order)))
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
    void getOrdersByBuyerIdShouldNotContainBuyerAndProductId() throws Exception {
       mockMvc.perform(get("/api/orders/buyer/1")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].buyer").doesNotExist())
               .andExpect(jsonPath("$[0].items[0].id").doesNotExist());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void getOrdersByBuyerIdWithoutPermissionShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders/buyer/1")).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void getOrdersByCurrentUserShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/orders/buyer")).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void deleteOrderWithAuthorizationReturnNoContent() throws Exception {
        Order order = createOrder();
        String postOrder = mockMvc.perform(request.builder(HttpMethod.POST, "/api/orders")
                                  .content(mapper.writeValueAsString(order)))
                                  .andExpect(status().isCreated())
                                  .andReturn().getResponse().getContentAsString();
        order = mapper.readValue(postOrder, Order.class);

        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/orders/{id}", order.getId())).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("user@test.pl")
    void deleteOrderWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/orders/{id}", 1L)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    void deleteOrderWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(request.builder(HttpMethod.DELETE, "/api/orders/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }
}