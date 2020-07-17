package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.OffsetDateTime;
import java.util.List;

@ApiModel(description = "User's single purchase. Can consist of multiple products.")
public class Transaction
{
	@ApiModelProperty(value = "Unique transaction idenfifier", readOnly = true, example = "1")
	private Long id;
	@ApiModelProperty(value = "User that make transaction. Cannot be null", required = true, position = 1)
	private User buyer;
	@ApiModelProperty(value = "List of products that user purchased. Cannot be empty or null.", required = true, position = 2)
	private List<Product> products;
	@ApiModelProperty(value = "Transaction time. Will be set automatically when posting transaction to DB.", readOnly = true, position = 3)
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
	
	public List<Product> getProducts()
	{
		return products;
	}
	
	public void setProducts(List<Product> products)
	{
		this.products = products;
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
	@ApiModelProperty(readOnly = true)
	public Double getCost()
	{
		return products.stream().map(Product::getPrice).reduce(0D, Double::sum);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Transaction)) return false;
		Transaction that = (Transaction) o;
		return getId().equals(that.getId());
	}
}