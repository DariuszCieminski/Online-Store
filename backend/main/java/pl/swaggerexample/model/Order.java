package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity(name = "Transaction")
@ApiModel(description = "User's single purchase. Can consist of multiple products.")
public class Order
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_sequence")
	@SequenceGenerator(name = "order_sequence", sequenceName = "order_sequence", allocationSize = 1)
	@ApiModelProperty(value = "Unique order idenfifier", readOnly = true, example = "1")
	private Long id;
	
	@Valid
	@NotNull(message = "Buyer is null.")
	@ManyToOne(optional = false)
	@JoinColumn(name = "buyer_id", nullable = false, updatable = false)
	@ApiModelProperty(value = "User that make order. Cannot be null", required = true, position = 1)
	private User buyer;
	
	@NotEmpty(message = "Product list is empty.")
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "order", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	@ApiModelProperty(value = "List of products that user purchased. Cannot be empty or null.", required = true, position = 2)
	@JsonIgnoreProperties("order")
	private Set<@Valid OrderItem> items;
	
	@CreationTimestamp
	@ApiModelProperty(value = "Time of making order. Will be set automatically when posting order to DB.", readOnly = true, position = 3)
	private OffsetDateTime time;
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public User getBuyer()
	{
		return buyer;
	}
	
	public void setBuyer(User buyer)
	{
		this.buyer = buyer;
	}
	
	public Set<OrderItem> getItems()
	{
		return items;
	}
	
	public void setItems(Set<OrderItem> products)
	{
		this.items = products;
		for (OrderItem item : items)
		{
			item.setOrder(this);
		}
	}
	
	public OffsetDateTime getTime()
	{
		return time;
	}
	
	public void setTime(OffsetDateTime time)
	{
		this.time = time;
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@ApiModelProperty(readOnly = true, value = "Total cost of the order.")
	public BigDecimal getCost()
	{
		return items.stream().map(orderItem -> orderItem.getProduct().getPrice().multiply(new BigDecimal(orderItem.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Order)) return false;
		Order that = (Order) o;
		return getId().equals(that.getId());
	}
}