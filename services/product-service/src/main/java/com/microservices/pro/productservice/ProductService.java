package com.microservices.pro.productservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ProductService — Session 1, Lab 1.
 *
 * In-memory store (no DB yet — see Session 1 homework for the JPA + PostgreSQL
 * upgrade). Implement the 5 TODOs below. See docs/labs/session-01-lab-01.md
 * for the full lab instructions.
 */
@Service
public class ProductService {

    // TODO 1: Inject a Map<Long, Product> as an in-memory store (no DB yet)
    //         Hint: use ConcurrentHashMap for thread safety.
    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // TODO 2: Implement findAll() returning List<Product>
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    // TODO 3: Implement findById(Long id) returning Optional<Product>
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    // TODO 4: Implement save(Product product) returning the saved Product
    //         Hint: if product.id() is null, assign one yourself before storing.
    public Product save(Product product) {
        Long id = product.id();
        if (id == null) {
            id = idGenerator.getAndIncrement();
            product = new Product(id, product.name(), product.description(), product.price(), product.category());
        }
        store.put(id, product);
        return product;
    }

    // TODO 5: Implement deleteById(Long id)
    public void deleteById(Long id) {
        store.remove(id);
    }

}
