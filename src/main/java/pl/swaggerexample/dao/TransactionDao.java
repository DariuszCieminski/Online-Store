package pl.swaggerexample.dao;

import org.springframework.data.repository.CrudRepository;
import pl.swaggerexample.model.Transaction;

public interface TransactionDao extends CrudRepository<Transaction, Long>
{
}