package pl.onlinestore.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.onlinestore.model.Product;
import pl.onlinestore.service.ProductService;

@RestController
@RequestMapping("/api/products")
@Api(tags = "Product controller",
     description = "Endpoints for getting, creating, editing and removing products offered for sale.")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ApiOperation(value = "Returns list of all products in the store. Can be filtered by request parameters.")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Price parameter is not a number")})
    public List<Product> getProducts(
        @RequestParam(value = "nameContains", required = false)
        @ApiParam(value = "Filters by product name containing this value.") String name,

        @RequestParam(value = "descContains", required = false)
        @ApiParam(value = "Filters by product description containing this value.") String desc,

        @RequestParam(value = "priceGreaterThan", required = false)
        @ApiParam(value = "Filters by product price greater than this value.", example = "10.99") BigDecimal priceGreater,

        @RequestParam(value = "priceLessThan", required = false)
        @ApiParam(value = "Filters by product price less than this value.", example = "10.99") BigDecimal priceLess,

        @RequestParam(value = "priceEqualTo", required = false)
        @ApiParam(value = "Filters by product price equal to this value.", example = "10.99") BigDecimal priceEqualTo) {

            Specification<Product> specification = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (name != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), String.format("%%%s%%", name)));
                }
                if (desc != null) {
                    predicates.add(criteriaBuilder.like(root.get("description"), String.format("%%%s%%", desc)));
                }
                if (priceGreater != null) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("price"), priceGreater));
                }
                if (priceLess != null) {
                    predicates.add(criteriaBuilder.lessThan(root.get("price"), priceLess));
                }
                if (priceEqualTo != null) {
                    predicates.add(criteriaBuilder.equal(root.get("price"), priceEqualTo));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            return productService.getProductsBySpecification(specification);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Returns a single product by its ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Product with specified ID doesn't exist")})
    public Product getProductById(@PathVariable @ApiParam(value = "Unique ID of existing product", example = "1") Long id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Adds new product to the database")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to add new product"),
                           @ApiResponse(code = 422, message = "Product has invalid data")})
    public Product addProduct(@Valid @RequestBody @ApiParam(value = "Data of the new product") Product product) {
        return productService.add(product);
    }

    @PutMapping
    @ApiOperation(value = "Updates an existing product")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to update product"),
                           @ApiResponse(code = 404, message = "Product with specified ID doesn't exist"),
                           @ApiResponse(code = 422, message = "Updated product has invalid data")})
    public Product updateProduct(@Valid @RequestBody @ApiParam(value = "Updated data of existing product") Product product) {
        return productService.update(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Removes a single product by its ID")
    @ApiResponses(value = {@ApiResponse(code = 403, message = "Non-manager is trying to delete product"),
                           @ApiResponse(code = 404, message = "Product with specified ID doesn't exist")})
    public void deleteProduct(@PathVariable @ApiParam(value = "Unique ID of existing product", example = "1") Long id) {
        productService.delete(id);
    }
}