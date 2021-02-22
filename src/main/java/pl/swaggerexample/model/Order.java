package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import pl.swaggerexample.model.enums.OrderStatus;
import pl.swaggerexample.model.enums.PaymentMethod;
import pl.swaggerexample.util.AddressConverter;
import pl.swaggerexample.util.JsonViews;

@Entity(name = "Transaction")
@ApiModel(description = "User's single purchase. Can consist of multiple products.")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_sequence")
    @SequenceGenerator(name = "order_sequence", sequenceName = "order_sequence", allocationSize = 1)
    @ApiModelProperty(value = "Unique order idenfifier", readOnly = true, example = "1")
    @JsonView(JsonViews.OrderSimple.class)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id", nullable = false, updatable = false)
    @ApiModelProperty(value = "User that make order. Cannot be null", required = true, position = 1)
    @JsonView(JsonViews.OrderDetailed.class)
    private User buyer;

    @NotEmpty(message = "Product list is empty.")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", orphanRemoval = true, cascade = {CascadeType.PERSIST,
                                                                                             CascadeType.REMOVE})
    @ApiModelProperty(value = "List of products that user purchased. Cannot be empty or null.", required = true,
                      position = 2)
    @JsonIgnoreProperties("order")
    @JsonView(JsonViews.OrderSimple.class)
    private Set<@Valid OrderItem> items;

    @Valid
    @NotNull(message = "Delivery address cannot be null.")
    @Convert(converter = AddressConverter.class)
    @ApiModelProperty(value = "Address the order should be delivered to.", required = true, position = 3)
    @JsonView(JsonViews.OrderSimple.class)
    private Address deliveryAddress;

    @NotNull(message = "Payment method was not set.")
    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Chosen method of payment for the order.", required = true, position = 4)
    @JsonView(JsonViews.OrderSimple.class)
    private PaymentMethod paymentMethod;

    @Size(max = 150)
    @ApiModelProperty(value = "Additional information to the order.", allowableValues = "range[-infinity, 150]", position = 5)
    @JsonView(JsonViews.OrderSimple.class)
    private String information;

    @Enumerated(EnumType.STRING)
    @ApiModelProperty(value = "Current status of the order.", required = true, position = 6)
    @JsonView(JsonViews.OrderSimple.class)
    private OrderStatus status;

    @CreationTimestamp
    @ApiModelProperty(value = "Time of making order. Will be set automatically when posting order to DB.", readOnly = true,
                      position = 7)
    @JsonView(JsonViews.OrderSimple.class)
    private OffsetDateTime time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public void setItems(Set<OrderItem> products) {
        this.items = products;
        for (OrderItem item : items) {
            item.setOrder(this);
        }
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Address deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ApiModelProperty(readOnly = true, value = "Total cost of the order.")
    @JsonView(JsonViews.OrderSimple.class)
    public BigDecimal getCost() {
        return items.stream()
                    .map(orderItem -> orderItem.getProduct().getPrice().multiply(new BigDecimal(orderItem.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        Order that = (Order) o;
        return getId().equals(that.getId());
    }
}