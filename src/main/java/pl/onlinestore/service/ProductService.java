package pl.onlinestore.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.onlinestore.dao.ProductDao;
import pl.onlinestore.exception.NotFoundException;
import pl.onlinestore.model.Product;

@Service
public class ProductService implements EntityService<Product> {

    private final ProductDao productDao;

    @Autowired
    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public Product getById(Long id) {
        return productDao.findById(id).orElseThrow(() -> new NotFoundException("There is no product with id: " + id));
    }

    @Override
    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        productDao.findAll().forEach(products::add);
        return products;
    }

    public List<Product> getProductsBySpecification(Specification<Product> productSpecification) {
        return productDao.findAll(productSpecification);
    }

    public Set<Product> getProductsByIds(Iterable<Long> ids) {
        Set<Product> products = new HashSet<>();
        productDao.findAllById(ids).forEach(products::add);
        return products;
    }

    @Override
    public Product add(Product object) {
        return productDao.save(object);
    }

    @Override
    public Product update(Product object) {
        if (object.getId() == null || !productDao.existsById(object.getId())) {
            throw new NotFoundException("Product doesn't exist.");
        }

        return productDao.save(object);
    }

    @Override
    public void delete(Long id) {
        Product product = getById(id);
        productDao.delete(product);
    }
}