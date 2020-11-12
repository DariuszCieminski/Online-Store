package pl.swaggerexample.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.swaggerexample.model.Address;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AddressConverter implements AttributeConverter<Address, String>
{
	private final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public String convertToDatabaseColumn(Address attribute)
	{
		try
		{
			return mapper.writeValueAsString(attribute);
		}
		catch (JsonProcessingException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}
	}
	
	@Override
	public Address convertToEntityAttribute(String dbData)
	{
		try
		{
			return mapper.readValue(dbData, Address.class);
		}
		catch (JsonProcessingException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}