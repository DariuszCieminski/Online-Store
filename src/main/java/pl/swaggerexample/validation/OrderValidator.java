package pl.swaggerexample.validation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import pl.swaggerexample.model.Order;
import pl.swaggerexample.model.OrderItem;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.service.ProductService;

public class OrderValidator implements Validator {

    private final ProductService productService;

    public OrderValidator(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Order.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Order order = (Order) target;

        Set<Long> orderedProductsIdsSet = order.getItems().stream()
                                               .filter(item -> item.getProduct().getId() != null)
                                               .map(item -> item.getProduct().getId())
                                               .collect(Collectors.toSet());

        if (orderedProductsIdsSet.size() != order.getItems().size()) {
            errors.rejectValue("items", "", "Order contains product with invalid ID.");
            return;
        }

        Map<Long, Integer> productQuantitiesMap =
            productService.getProductsByIds(orderedProductsIdsSet).stream()
                          .collect(Collectors.toMap(Product::getId, Product::getQuantity));

        int itemIndex = 0;

        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            Integer currentProductQuantity = productQuantitiesMap.get(product.getId());

            if (currentProductQuantity == null) {
                errors.rejectValue("items[" + itemIndex + "]", "",
                                   String.format("Product with ID=%s doesn't exist.", product.getId()));
            } else if (orderItem.getQuantity() > currentProductQuantity) {
                String errorMsg = String.format("Product '%s' is ordered in more quantity (%d) than is in stock (%d).",
                                                product.getName(), orderItem.getQuantity(), currentProductQuantity);
                errors.rejectValue("items[" + itemIndex + "].quantity", "", errorMsg);
            }

            itemIndex++;
        }
    }
}