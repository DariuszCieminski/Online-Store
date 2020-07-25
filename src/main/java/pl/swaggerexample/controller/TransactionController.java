package pl.swaggerexample.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.swaggerexample.model.Transaction;
import pl.swaggerexample.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Api(description = "Endpoints for getting, adding and removing transactions that users make.")
public class TransactionController
{
	private final TransactionService transactionService;
	
	@Autowired
	public TransactionController(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}
	
	@GetMapping
	@ApiOperation(value = "Returns list of all made transactions")
	public List<Transaction> getTransactions()
	{
		return transactionService.getAll();
	}
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Returns a single transaction by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Transaction with specified ID doesn't exist")})
	public Transaction getTransaction(@PathVariable @ApiParam(value = "Unique ID of existing transaction", example = "1") Long id)
	{
		return transactionService.getById(id);
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(value = "Adds new transaction to database")
	@ApiResponses(value = {@ApiResponse(code = 422, message = "Transaction has invalid data")})
	public Transaction addTransaction(@RequestBody @ApiParam(value = "Data of the new transaction") Transaction transaction)
	{
		return transactionService.add(transaction);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value = "Removes a single transaction by its ID")
	@ApiResponses(value = {@ApiResponse(code = 404, message = "Transaction with specified ID doesn't exist")})
	public void deleteTransaction(@PathVariable @ApiParam(value = "Unique ID of existing transaction", example = "1") Long id)
	{
		transactionService.delete(id);
	}
}