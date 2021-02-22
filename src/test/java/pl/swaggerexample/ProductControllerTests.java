package pl.swaggerexample;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.dao.UserDao;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.model.User;
import pl.swaggerexample.model.enums.Role;

@SpringBootTest(classes = SwaggerExampleApplication.class)
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class ProductControllerTests {

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
            new Product("Milk", "A carton of milk.", Collections.singleton("/milkimageurl"), BigDecimal.valueOf(4.99D), 20),
            new Product("Bread", "A single loaf of bread.", Collections.singleton("/breadimageurl"),
                        BigDecimal.valueOf(2.79D), 32),
            new Product("Ham", "250g of ham.", Collections.singleton("/hamimageurl"), BigDecimal.valueOf(10.50D), 16),
            new Product("Coffee", "A packet of coffee.", Collections.singleton("/coffeeimageurl"),
                        BigDecimal.valueOf(15.00D), 10),
            new Product("Butter", "500g of butter.", Collections.singleton("/butterimageurl"), BigDecimal.valueOf(7.29D), 8));

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

    @Test
    @WithUserDetails("manager@test.pl")
    public void createProductReturnOk() throws Exception {
        Product product = new Product("Product 1", "Simple description of Product 1",
                                      Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(11.99D), 3);

        mockMvc.perform(post("/api/products")
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
    @WithUserDetails("manager@test.pl")
    public void createProductWithoutNameReturnUnprocessableEntity() throws Exception {
        Product product = new Product("", "Simple description of Product 1",
                                      Collections.singleton("https://picsum.photos/200"), BigDecimal.valueOf(11.99D), 1);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void createProductWithInvalidImageUrlReturnUnprocessableEntity() throws Exception {
        Product product = new Product("Product", "Simple description of Product 1", Collections.singleton("image_url"),
                                      BigDecimal.valueOf(11.99D), 1);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void createProductWithNegativePriceReturnUnprocessableEntity() throws Exception {
        Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"),
                                      BigDecimal.valueOf(-8.99D), 1);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void createProductWithPriceWithThreeDecimalPlacesReturnUnprocessableEntity() throws Exception {
        Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"),
                                      BigDecimal.valueOf(8.999D), 1);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void createProductWithNegativeQuantityReturnUnprocessableEntity() throws Exception {
        Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"),
                                      BigDecimal.valueOf(-8.99D), -5);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void createProductByNonManagerReturnForbidden() throws Exception {
        Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"),
                                      BigDecimal.valueOf(11.99D), 1);

        mockMvc.perform(post("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getProductByValidIdReturnOk() throws Exception {
        Long id = 1L;
        mockMvc.perform(get("/api/products/{id}", id)).andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getProductByInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @WithUserDetails("user@test.pl")
    @MethodSource("getRequestParams")
    public void getProductsFilteredByCustomPredicatesReturnOk(SimpleImmutableEntry<String, String> param) throws Exception {
        mockMvc.perform(get("/api/products").queryParam(param.getKey(), param.getValue()))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", Matchers.hasSize(Matchers.greaterThanOrEqualTo(1))));
    }

    private static List<SimpleImmutableEntry<String, String>> getRequestParams() {
        return Arrays.asList(new SimpleImmutableEntry<>("nameContains", "Milk"),
                             new SimpleImmutableEntry<>("descContains", "packet"),
                             new SimpleImmutableEntry<>("priceGreaterThan", "6.00"),
                             new SimpleImmutableEntry<>("priceLessThan", "10.50"),
                             new SimpleImmutableEntry<>("priceEqualTo", "7.29"));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void getProductsFilteredByCustomPredicateWithWrongValueReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/products").queryParam("priceEqualTo", "price"))
               .andDo(print())
               .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductReturnOk() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setName("Updated product");
        product.setDescription("Updated description");
        product.setPrice(BigDecimal.valueOf(5.99D));

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value(product.getName()))
               .andExpect(jsonPath("$.description").value(product.getDescription()))
               .andExpect(jsonPath("$.price").value(product.getPrice()));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithInvalidIdReturnNotFound() throws Exception {
        Product product = new Product("Product", "Description", Collections.singleton("https://picsum.photos/200"),
                                      BigDecimal.valueOf(3.99D), 1);
        product.setId(333L);

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithEmptyNameReturnUnprocessableEntity() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setName(null);

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithInvalidImageUrlReturnUnprocessableEntity() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.getImages().add("image_url");

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("images[]"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithNegativePriceReturnUnprocessableEntity() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setPrice(BigDecimal.valueOf(-8.99D));

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("price"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithPriceWithThreeDecimalPlacesReturnUnprocessableEntity() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setPrice(BigDecimal.valueOf(5.999D));

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("price"));
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void updateProductWithNegativeQuantityReturnUnprocessableEntity() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setQuantity(-2);

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isUnprocessableEntity())
               .andExpect(jsonPath("$.errors").isArray())
               .andExpect(jsonPath("$.errors", hasSize(1)))
               .andExpect(jsonPath("$.errors[0].field").value("quantity"));
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void updateProductWithoutAuthorizationReturnForbidden() throws Exception {
        String productJson = mockMvc.perform(get("/api/products/1"))
                                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Product product = mapper.readValue(productJson, Product.class);
        product.setName("Updated product name");

        mockMvc.perform(put("/api/products")
               .content(mapper.writeValueAsString(product)).contentType(MediaType.APPLICATION_JSON)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteProductWithAuthorizationReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1L)).andDo(print())
               .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("user@test.pl")
    public void deleteProductWithoutAuthorizationReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 1L)).andDo(print())
               .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@test.pl")
    public void deleteProductWithInvalidIdReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999L)).andDo(print())
               .andExpect(status().isNotFound());
    }
}