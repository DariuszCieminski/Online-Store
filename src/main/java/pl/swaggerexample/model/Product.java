package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import pl.swaggerexample.util.JsonViews.OrderDetailed;
import pl.swaggerexample.util.JsonViews.OrderSimple;

@Entity
@ApiModel(description = "Product, that is offered to buy in the store.")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @SequenceGenerator(name = "product_sequence", sequenceName = "product_sequence", allocationSize = 1)
    @ApiModelProperty(value = "Unique product identifier", example = "1")
    @JsonView(OrderDetailed.class)
    private Long id;

    @NotBlank(message = "Product name cannot be empty.")
    @ApiModelProperty(value = "Name of the product", required = true, example = "A carton of milk", position = 1)
    @JsonView(OrderSimple.class)
    private String name;

    @ApiModelProperty(value = "Short description of the product.", position = 2)
    @JsonView(OrderDetailed.class)
    private String description;

    @ElementCollection
    @Column(name = "image")
    @ApiModelProperty(value = "Links to product's images.", example = "['http://picsum.photos/200', '/link']", position = 5)
    @JsonView(OrderSimple.class)
    private Set<@Pattern(regexp = "^(http|https)?.*/.*") String> images;

    @NotNull(message = "Product price was not specified.")
    @Positive(message = "Price must be greater than zero.")
    @Digits(integer = 10, fraction = 2, message = "Price can have a precision of 2 decimal places at most.")
    @ApiModelProperty(value = "Price of the product. Must be positive and have a precision of 2 decimal places.",
                      required = true, example = "11.99", position = 3)
    @JsonView(OrderSimple.class)
    private BigDecimal price;

    @NotNull(message = "Product quantity was not specified.")
    @PositiveOrZero(message = "Product quantity must not be less than zero.")
    @ApiModelProperty(value = "Quantity of the product available in stock. Must not be less than zero.", required = true,
                      example = "30", position = 4)
    private Integer quantity;

    public Product() {
    }

    public Product(String name, String description, Set<String> images, BigDecimal price, Integer quantity) {
        this.name = name;
        this.description = description;
        this.images = images;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getImages() {
        return images;
    }

    public void setImages(Set<String> images) {
        this.images = images;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product)) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(getId(), product.getId());
    }
}