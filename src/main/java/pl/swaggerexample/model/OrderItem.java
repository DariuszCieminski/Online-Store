package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import pl.swaggerexample.util.JsonViews.OrderDetailed;
import pl.swaggerexample.util.JsonViews.OrderSimple;

@Entity
@ApiModel(description = "Product inside the cart.")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_sequence")
    @SequenceGenerator(name = "order_item_sequence", sequenceName = "order_item_sequence", allocationSize = 1)
    @ApiModelProperty(value = "Unique order item identifier.", readOnly = true, example = "1")
    @JsonView(OrderDetailed.class)
    private Long id;

    @Valid
    @NotNull(message = "Product is null.")
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    @ApiModelProperty(value = "Product from stock. Must not be null.", required = true)
    @JsonView(OrderSimple.class)
    private Product product;

    @NotNull(message = "Product quantity is null.")
    @Positive(message = "Product quantity must be greater than zero.")
    @ApiModelProperty(value = "Quantity of given product purchased by user.", required = true, example = "5")
    @JsonView(OrderSimple.class)
    private Integer quantity;

    @NotNull(message = "Order is null.")
    @ManyToOne
    @JoinColumn(name = "order_id", updatable = false)
    @ApiModelProperty(value = "Order, which this item is part of. Must not be null.", required = true)
    private Order order;

    public OrderItem() {
    }

    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer amount) {
        this.quantity = amount;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}