package pl.swaggerexample.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.swaggerexample.exception.NotFoundException;

import java.util.*;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler
{
	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<String> handleNotFoundException(NotFoundException e)
	{
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request)
	{
		List<Map<String, String>> errorList = new ArrayList<>();
		
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors())
		{
			Map<String, String> errorMap = new HashMap<>();
			errorMap.put("field", fieldError.getField());
			errorMap.put("error", fieldError.getDefaultMessage());
			errorList.add(errorMap);
		}
		
		Map<String, List<Map<String, String>>> output = Collections.singletonMap("errors", errorList);
		
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(output);
	}
}