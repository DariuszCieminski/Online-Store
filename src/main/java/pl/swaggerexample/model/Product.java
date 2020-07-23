package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@ApiModel(description = "Product, which is offered to buy on the shop.")
public class Product
{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
	@SequenceGenerator(name = "product_sequence", sequenceName = "product_sequence", allocationSize = 1)
	@ApiModelProperty(value = "Unique product identifier", example = "1")
	private Long id;
	
	@NotBlank(message = "Product name cannot be empty.")
	@ApiModelProperty(value = "Name of the product", required = true, example = "A carton of milk", position = 1)
	private String name;
	
	@ApiModelProperty(value = "Short description of the product.", position = 2)
	private String description;
	
	@URL
	@ApiModelProperty(value = "Link to product's image.", example = "http://picsum.photos/200", position = 4)
	private String imageUrl;
	
	@NotNull(message = "Product price was not specified.")
	@Positive(message = "Price must be greater than zero.")
	@Digits(integer = 10, fraction = 2, message = "Price can have a precision of 2 decimal places at most.")
	@ApiModelProperty(value = "Price of the product. Must be positive and have a precision of 2 decimal places.", required = true, example = "11.99", position = 3)
	private BigDecimal price;
	
	public Product()
	{
	}
	
	public Product(String name, String description, String imageUrl, BigDecimal price)
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
	
	public BigDecimal getPrice()
	{
		return price;
	}
	
	public void setPrice(BigDecimal price)
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