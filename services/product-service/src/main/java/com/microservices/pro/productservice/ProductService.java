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

    // TODO 2: Implement findAll() returning List<Product>

    // TODO 3: Implement findById(Long id) returning Optional<Product>

    // TODO 4: Implement save(Product product) returning the saved Product
    //         Hint: if product.id() is null, assign one yourself before storing.

    // TODO 5: Implement deleteById(Long id)

}
