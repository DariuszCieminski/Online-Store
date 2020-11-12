package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Entity
@ApiModel(description = "Product inside the cart.")
public class OrderItem
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_item_sequence")
	@SequenceGenerator(name = "order_item_sequence", sequenceName = "order_item_sequence", allocationSize = 1)
	@ApiModelProperty(value = "Unique order item identifier.", readOnly = true, example = "1")
	private Long id;
	
	@Valid
	@NotNull(message = "Product is null.")
	@OneToOne
	@JoinColumn(name = "product_id", nullable = false, updatable = false)
	@ApiModelProperty(value = "Product from stock. Must not be null.", required = true)
	private Product product;
	
	@NotNull(message = "Product quantity is null.")
	@Positive(message = "Product quantity must be greater than zero.")
	@ApiModelProperty(value = "Quantity of given product purchased by user.", required = true, example = "5")
	private Integer quantity;
	
	@NotNull(message = "Order is null.")
	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false, updatable = false)
	@ApiModelProperty(value = "Order, which this item is part of. Must not be null.", required = true)
	private Order order;
	
	public OrderItem()
	{
	}
	
	public OrderItem(Product product, Integer quantity)
	{
		this.product = product;
		this.quantity = quantity;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public Product getProduct()
	{
		return product;
	}
	
	public void setProduct(Product product)
	{
		this.product = product;
	}
	
	public Integer getQuantity()
	{
		return quantity;
	}
	
	public void setQuantity(Integer amount)
	{
		this.quantity = amount;
	}
	
	public Order getOrder()
	{
		return order;
	}
	
	public void setOrder(Order order)
	{
		this.order = order;
	}
}