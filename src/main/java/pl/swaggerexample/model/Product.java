package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(description = "Product, which is offered to buy on the shop.")
public class Product
{
	@ApiModelProperty(value = "Unique product identifier", example = "1")
	private Long id;
	@ApiModelProperty(value = "Name of the product", required = true, example = "A carton of milk", position = 1)
	private String name;
	@ApiModelProperty(value = "Short description of the product.", position = 2)
	private String description;
	@ApiModelProperty(value = "Link to product's image.", example = "http://picsum.photos/200", position = 4)
	private String imageUrl;
	@ApiModelProperty(value = "Price of the product. Must have a precision of 2 digits.", required = true, example = "11.99", position = 3)
	private Double price;
	
	public Product(String name, String description, String imageUrl, Double price)
	{
		this.name = name;
		this.description = description;
		this.imageUrl = imageUrl;
		this.price = price;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getImageUrl()
	{
		return imageUrl;
	}
	
	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}
	
	public Double getPrice()
	{
		return price;
	}
	
	public void setPrice(Double price)
	{
		this.price = price;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Product)) return false;
		Product product = (Product) o;
		return Objects.equals(getId(), product.getId());
	}
}