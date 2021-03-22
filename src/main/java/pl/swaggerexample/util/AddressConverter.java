package pl.swaggerexample.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import pl.swaggerexample.model.Address;

@Converter
public class AddressConverter implements AttributeConverter<Address, String> {

    private final ObjectMapper mapper;

    public AddressConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String convertToDatabaseColumn(Address attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public Address convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, Address.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}