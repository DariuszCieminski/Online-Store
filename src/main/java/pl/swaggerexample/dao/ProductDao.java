package pl.swaggerexample.dao;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.Product;

public interface ProductDao extends CrudRepository<Product, Long>, JpaSpecificationExecutor<Product>
{
}