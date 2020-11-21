package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.ProductDao;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Product;

import java.util.ArrayList;
import java.util.List;

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
	
	public List<Product> getProductsByPredicates(Specification<Product> productSpecification)
	{
		return productDao.findAll(productSpecification);
	}
	
	public Iterable<Product> getProductsByIds(Iterable<Long> ids)
	{
		return productDao.findAllById(ids);
	}
	
	@Override
	public Product add(Product object)
	{
		return productDao.save(object);
	}
	
	@Override
	public Product update(Product object)
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
}