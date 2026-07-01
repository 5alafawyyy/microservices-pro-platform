package com.microservices.pro.productservice;

import org.springframework.web.bind.annotation.*;

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

    // TODO: GET /api/v1/products/{id}    → return product by id (404 if not found)

    // TODO: POST /api/v1/products        → create a new product (201 Created)

    // TODO: DELETE /api/v1/products/{id} → delete a product

}
