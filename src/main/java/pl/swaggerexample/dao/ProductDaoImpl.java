package pl.swaggerexample.dao;

import org.springframework.stereotype.Repository;
import pl.swaggerexample.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductDaoImpl implements Dao<Product>
{
	private List<Product> products = new ArrayList<>();
	
	@Override
	public Optional<Product> getById(Long id)
	{
		return products.stream().filter(product -> product.getId().equals(id)).findFirst();
	}
	
	@Override
	public List<Product> getAll()
	{
		return products;
	}
	
	@Override
	public Product save(Product product)
	{
		if (!products.isEmpty())
		{
			Product p = products.get(products.size() - 1);
			product.setId(p.getId() + 1);
		}
		else product.setId(1L);
		products.add(product);
		
		return product;
	}
	
	@Override
	public Product update(Product product)
	{
		return products.set(products.indexOf(product), product);
	}
	
	@Override
	public void delete(Product product)
	{
		products.remove(product);
	}
}