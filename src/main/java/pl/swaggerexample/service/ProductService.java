package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.ProductDaoImpl;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Product;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ProductService
{
	private final ProductDaoImpl productDao;
	
	@Autowired
	public ProductService(ProductDaoImpl productDao) {this.productDao = productDao;}
	
	public List<Product> getAllProducts(List<Predicate<Product>> predicates)
	{
		List<Product> products = productDao.getAll();
		if (predicates == null || predicates.isEmpty()) return products;
		
		return products.stream().filter(predicates.stream().reduce(p -> true, Predicate::and)).collect(Collectors.toList());
	}
	
	public Product getProductById(Long id)
	{
		return productDao.getById(id).orElseThrow(() -> new NotFoundException("There is no product with id: " + id));
	}
	
	public Product addProduct(Product product)
	{
		validateProduct(product);
		return productDao.save(product);
	}
	
	public Product updateProduct(Product product)
	{
		if (getAllProducts(null).stream().noneMatch(p -> p.getId().equals(product.getId())))
			throw new NotFoundException("Product doesn't exist!");
		
		validateProduct(product);
		
		return productDao.update(product);
	}
	
	public void deleteProduct(Long productId)
	{
		Product product = getProductById(productId);
		productDao.delete(product);
	}
	
	private void validateProduct(Product product)
	{
		if (product.getName().isEmpty()) throw new IllegalArgumentException("Product name cannot be empty!");
		if (product.getPrice() < 0) throw new IllegalArgumentException("Price must be greater than zero!");
	}
}