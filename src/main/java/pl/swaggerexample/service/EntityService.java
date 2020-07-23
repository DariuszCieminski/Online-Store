package pl.swaggerexample.service;

import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.List;

public interface EntityService<T>
{
	T getById(Long id);
	
	List<T> getAll();
	
	T add(@Valid T object, BindingResult result);
	
	T update(@Valid T object, BindingResult result);
	
	void delete(Long id);
}