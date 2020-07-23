package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Product;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ProductService implements EntityService<Product>
{
	private final ProductDao productDao;
	
	@Autowired
	public ProductService(ProductDao productDao)
	{
		this.productDao = productDao;
	}
	
	@Override
	public Product getById(Long id)
	{
		return productDao.findById(id).orElseThrow(() -> new NotFoundException("There is no product with id: " + id));
	}
	
	@Override
	public List<Product> getAll()
	{
		List<Product> products = new ArrayList<>();
		productDao.findAll().forEach(products::add);
		return products;
	}
	
	@Override
	public Product add(@Valid Product object, BindingResult result)
	{
		return productDao.save(object);
	}
	
	@Override
	public Product update(@Valid Product object, BindingResult result)
	{
		if (getAll().stream().noneMatch(p -> p.getId().equals(object.getId())))
			throw new NotFoundException("Product doesn't exist.");
		return productDao.save(object);
	}
	
	@Override
	public void delete(Long id)
	{
		Product product = getById(id);
		productDao.delete(product);
	}
	
	public List<Product> getProductsByPredicates(List<Predicate<Product>> predicates)
	{
		List<Product> products = getAll();
		if (predicates == null || predicates.isEmpty()) return products;
		
		return products.stream().filter(predicates.stream().reduce(p -> true, Predicate::and)).collect(Collectors.toList());
	}
}