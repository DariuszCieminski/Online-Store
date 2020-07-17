package pl.swaggerexample.dao;

import org.springframework.stereotype.Repository;
import pl.swaggerexample.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionDaoImpl implements Dao<Transaction>
{
	private List<Transaction> transactions = new ArrayList<>();
	
	@Override
	public Optional<Transaction> getById(Long id)
	{
		return transactions.stream().filter(transaction -> transaction.getId().equals(id)).findFirst();
	}
	
	@Override
	public List<Transaction> getAll()
	{
		return transactions;
	}
	
	@Override
	public Transaction save(Transaction transaction)
	{
		if (!transactions.isEmpty())
		{
			Transaction t = transactions.get(transactions.size() - 1);
			transaction.setId(t.getId() + 1);
		}
		else transaction.setId(1L);
		transactions.add(transaction);
		
		return transaction;
	}
	
	@Override
	public Transaction update(Transaction transaction)
	{
		return null;
	}
	
	@Override
	public void delete(Transaction transaction)
	{
		transactions.remove(transaction);
	}
}