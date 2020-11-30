package pl.swaggerexample.validation;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.service.ProductService;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OrderValidator implements Validator
{
	private final ProductService productService;
	
	public OrderValidator(ProductService productService)
	{
		this.productService = productService;
	}
	
	@Override
	public boolean supports(Class<?> clazz)
	{
		return Order.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors)
	{
		Order order = (Order) target;
		
		Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (order.getBuyer() == null || !userId.equals(order.getBuyer().getId()))
		{
			errors.rejectValue("buyer", "", "Invalid buyer.");
		}
		
		int itemIndex = 0;
		Set<Long> orderedProductsIds = order.getItems().stream().filter(item -> item.getProduct().getId() != null)
																.map(item -> item.getProduct().getId()).collect(Collectors.toSet());
		Map<Long, Integer> productQuantitiesFetchedFromDb = StreamSupport.stream(productService.getProductsByIds(orderedProductsIds).spliterator(), false)
		                                                                 .collect(Collectors.toMap(Product::getId, Product::getQuantity));
		
		if (orderedProductsIds.size() != order.getItems().size())
		{
			errors.rejectValue("items", "", "Order contains product with no ID.");
		}
		
		for (OrderItem orderItem : order.getItems())
		{
			Integer productQuantityFromDb = productQuantitiesFetchedFromDb.get(orderItem.getProduct().getId());
			
			if (productQuantityFromDb == null)
			{
				errors.rejectValue("items[" + itemIndex + "]", "", String.format("Product with ID=%s doesn't exist.", orderItem.getProduct().getId()));
			}
			
			else if (orderItem.getQuantity() > productQuantityFromDb)
			{
				String error = String.format("Product '%s' is ordered in more quantity (%d) than is in stock (%d).",
											 orderItem.getProduct().getName(), orderItem.getQuantity(), productQuantityFromDb);
				errors.rejectValue("items[" + itemIndex + "].quantity", "", error);
			}
			
			itemIndex++;
		}
	}
}