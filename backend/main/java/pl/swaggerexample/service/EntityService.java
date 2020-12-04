package pl.swaggerexample.service;

import java.util.List;

public interface EntityService<T> {

    T getById(Long id);

    List<T> getAll();

    T add(T object);

    T update(T object);

    void delete(Long id);
}