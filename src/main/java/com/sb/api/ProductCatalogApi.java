package com.sb.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sb.domain.Product;
import com.sb.service.ProductCatalogServiceInterface;

@RestController
@RequestMapping("/api")
public class ProductCatalogApi {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogApi.class);
    private final ProductCatalogServiceInterface service;

    public ProductCatalogApi(ProductCatalogServiceInterface service) {
        this.service = service;
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        logger.info("POST /api/products - Adding product: {}", product != null ? product.getName() : "null");
        return ResponseEntity.ok(service.addProduct(product));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        logger.info("GET /api/products - Fetching products: page {}, size {}", page, size);
        if (page < 0) {
            logger.warn("Page parameter was negative: {}", page);
            page = 0;
        }
        if (size <= 0 || size > 100) {
            logger.warn("Size parameter out of bounds: {}, defaulting to 10", size);
            size = 10;
        }
        List<Product> products = service.getProducts(page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        logger.info("GET /api/products/{} - Fetching product by ID", id);
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Product ID was null or empty");
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        logger.info("GET /api/products/{} - Fetching product", id);
        Product product = service.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        logger.info("GET /api/search - Searching for products with query: {}", q);
        List<Product> results = service.searchProducts(q);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/products/prepopulate")
    public ResponseEntity<String> prepopulateProducts() {
        logger.info("POST /api/products/prepopulate - Reloading product data from JSON");
        service.prepopulateProducts();
        return ResponseEntity.ok("Product data reloaded from JSON.");
    }
}
