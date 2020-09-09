package pl.swaggerexample.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.Product;
import pl.swaggerexample.service.ProductService;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Api(description = "Endpoints for getting, creating, editing and removing products offered for sale.")
public class ProductController
{
	private final ProductService productService;
	
	@Autowired
	public ProductController(ProductService productService)
	{
		this.productService = productService;
	}
	
	@GetMapping
	@ApiOperation(value = "Returns list of all products in the shop. Can be filtered by request parameters.")
	@ApiResponses(value = {@ApiResponse(code = 400, message = "Price parameter is not a number")})
	public List<Product> getProducts(@RequestParam(value = "nameContains", required = false) @ApiParam(value = "Filters by product name containing this value.") String name,
	                                 @RequestParam(value = "descContains", required = false) @ApiParam(value = "Filters by product description containing this value.") String desc,
	                                 @RequestParam(value = "priceGreaterThan", required = false) @ApiParam(value = "Filters by product price greater than this value.", example = "10.99") BigDecimal priceGreater,
	                                 @RequestParam(value = "priceLessThan", required = false) @ApiParam(value = "Filters by product price less than this value.", example = "10.99") BigDecimal priceLess,
	                                 @RequestParam(value = "priceEqualTo", required = false) @ApiParam(value = "Filters by product price equal to this value.", example = "10.99") BigDecimal priceEqualTo)
	{
		Specification<Product> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			
			if (name != null) predicates.add(criteriaBuilder.like(root.get("name"), String.format("%%%s%%", name)));
			if (desc != null) predicates.add(criteriaBuilder.like(root.get("description"), String.format("%%%s%%", desc)));
			if (priceGreater != null) predicates.add(criteriaBuilder.greaterThan(root.get("price"), priceGreater));
			if (priceLess != null) predicates.add(criteriaBuilder.lessThan(root.get("price"), priceLess));
			if (priceEqualTo != null) predicates.add(criteriaBuilder.equal(root.get("price"), priceEqualTo));
			
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		
		return productService.getProductsByPredicates(specification);
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Returns single product by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Product with specified ID doesn't exist")})
	public Product getProduct(@PathVariable @ApiParam(value = "Unique ID of existing product", example = "1") Long id)
	{
		return productService.getById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Adds new product to the database")
	@ApiResponses(value = {@ApiResponse(code = 422, message = "Product has invalid data")})
	public Product addProduct(@Valid @RequestBody @ApiParam(value = "Data of the new product") Product product)
	{
		return productService.add(product);
	}
	
	@PutMapping
	@ApiOperation(value = "Updates an existing product")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Product with specified ID doesn't exist"), @ApiResponse(code = 422, message = "Updated product has invalid data")})
	public Product updateProduct(@Valid @RequestBody @ApiParam(value = "Updated data of existing product") Product product)
	{
		return productService.update(product);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Removes a single product by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Product with specified ID doesn't exist")})
	public void deleteProduct(@PathVariable @ApiParam(value = "Unique ID of existing product", example = "1") Long id)
	{
		productService.delete(id);
	}
}