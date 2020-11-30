package pl.swaggerexample.model;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import pl.swaggerexample.util.JsonViews;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@ApiModel(description = "Delivery address of shop's customer. Needed for sending user orders.")
public class Address
{
	@NotBlank
	@JsonView({JsonViews.UserAuthentication.class, JsonViews.OrderSimple.class})
	private String street;
	
	@NotBlank
	@Pattern(regexp = "^[0-9]{2}-[0-9]{3}$")
	@ApiModelProperty(example = "01-234")
	@JsonView({JsonViews.UserAuthentication.class, JsonViews.OrderSimple.class})
	private String postCode;
	
	@NotBlank
	@JsonView({JsonViews.UserAuthentication.class, JsonViews.OrderSimple.class})
	private String city;
	
	public Address()
	{
	}
	
	public Address(String street, String postCode, String city)
	{
		this.street = street;
		this.postCode = postCode;
		this.city = city;
	}
	
	public String getStreet()
	{
		return street;
	}
	
	public void setStreet(String street)
	{
		this.street = street;
	}
	
	public String getPostCode()
	{
		return postCode;
	}
	
	public void setPostCode(String postCode)
	{
		this.postCode = postCode;
	}
	
	public String getCity()
	{
		return city;
	}
	
	public void setCity(String city)
	{
		this.city = city;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(getStreet(), getPostCode(), getCity());
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Address)) return false;
		Address address = (Address) o;
		return getStreet().equals(address.getStreet()) &&
				getPostCode().equals(address.getPostCode()) &&
				getCity().equals(address.getCity());
	}
}