package com.microservices.pro.productservice;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * ProductController — Session 1, Lab 1.
 *
 * Implement the endpoints below. See docs/labs/session-01-lab-01.md for the
 * full lab instructions and acceptance criteria.
 *
 *   GET    /api/v1/products       → all products
 *   GET    /api/v1/products/{id}  → product by id (404 if not found)
 *   POST   /api/v1/products       → create a new product (201 Created)
 *   DELETE /api/v1/products/{id}  → delete a product
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // TODO: GET /api/v1/products         → return all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    // TODO: GET /api/v1/products/{id}    → return product by id (404 if not found)
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO: POST /api/v1/products        → create a new product (201 Created)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // TODO: DELETE /api/v1/products/{id} → delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
