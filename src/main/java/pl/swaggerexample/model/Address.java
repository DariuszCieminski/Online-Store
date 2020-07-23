package pl.swaggerexample.model;

import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ApiModel(description = "Home address of shop's customer. Needed for sending user's purchases.")
public class Address
{
	@NotBlank
	private String street;
	
	@NotBlank
	@Pattern(regexp = "^[0-9]{2}-[0-9]{3}$")
	private String postCode;
	
	@NotBlank
	private String city;
	
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
}