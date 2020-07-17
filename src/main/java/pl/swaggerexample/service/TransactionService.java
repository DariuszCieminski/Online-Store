package pl.swaggerexample.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.swaggerexample.dao.TransactionDaoImpl;
import pl.swaggerexample.exception.NotFoundException;
import pl.swaggerexample.model.Transaction;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransactionService
{
	private final TransactionDaoImpl transactionDao;
	
	@Autowired
	public TransactionService(TransactionDaoImpl transactionDao) {this.transactionDao = transactionDao;}
	
	public List<Transaction> getAllTransactions()
	{
		return transactionDao.getAll();
	}
	
	public Transaction getTransactionById(Long id)
	{
		return transactionDao.getById(id).orElseThrow(() -> new NotFoundException("There is no transaction with id: " + id));
	}
	
	public Transaction addTransaction(Transaction transaction)
	{
		validateTransaction(transaction);
		transaction.setTime(OffsetDateTime.now());
		
		return transactionDao.save(transaction);
	}
	
	public void deleteTransaction(Long transactionId)
	{
		Transaction transaction = getTransactionById(transactionId);
		transactionDao.delete(transaction);
	}
	
	private void validateTransaction(Transaction transaction)
	{
		if (getAllTransactions().stream().anyMatch(t -> t.getId().equals(transaction.getId())))
			throw new IllegalArgumentException("There is already a transaction with id: " + transaction.getId());
		
		if (transaction.getBuyer() == null || transaction.getBuyer().getId() == null)
			throw new IllegalArgumentException("Invalid buyer.");
		
		if (transaction.getProducts().isEmpty()) throw new IllegalArgumentException("Product list is empty!");
	}
}