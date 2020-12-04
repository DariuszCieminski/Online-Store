package pl.swaggerexample.dao;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.Product;

public interface ProductDao extends CrudRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = "images")
    Iterable<Product> findAll();

    @Override
    @EntityGraph(attributePaths = "images")
    Iterable<Product> findAllById(Iterable<Long> longs);

    @Override
    @EntityGraph(attributePaths = "images")
    List<Product> findAll(Specification<Product> spec);
}