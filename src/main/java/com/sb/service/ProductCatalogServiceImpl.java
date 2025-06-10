package com.sb.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.domain.Product;
import com.sb.repository.ProductCatalogRepositoryInterface;

@Service
public class ProductCatalogServiceImpl implements ProductCatalogServiceInterface {

    private static final Logger logger = LoggerFactory.getLogger(ProductCatalogServiceImpl.class);

    private final ProductCatalogRepositoryInterface productRepository;

    private final ObjectMapper objectMapper;

    public ProductCatalogServiceImpl(ProductCatalogRepositoryInterface productRepository, 
    ObjectMapper objectMapper) {

        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }


    public Product addProduct(Product product) {
        if (product == null) {
            logger.warn("Received null product in addProduct");
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            logger.warn("Product name is null or empty");
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }

        if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
            logger.warn("Product category is null or empty");
            throw new IllegalArgumentException("Product category cannot be null or empty");
        }

        if (product.getPrice() < 0) {
            logger.warn("Product price is negative: {}", product.getPrice());
            throw new IllegalArgumentException("Product price cannot be negative");
        }

        if (product.getImageUrl() == null || product.getImageUrl().trim().isEmpty()) {
            logger.warn("Product image url is null or empty");
            throw new IllegalArgumentException("Product image url cannot be null or empty");
        }
        return productRepository.save(product);
    }

    public List<Product> getProducts(int page, int size) {
        List<Product> all = productRepository.findAll();
        int start = page * size;
        if (start >= all.size()) {
            logger.warn("Requested page {} with size {} exceeds available products", page, size);
            return List.of();
        }
        int end = Math.min(start + size, all.size());
        return all.subList(start, end);
    }

    public Product getProductById(String id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }

        String normalizedQuery = query.toLowerCase();
        return productRepository.findAll().stream()
        .sorted(Comparator
            .comparingInt((Product p) -> {
                String name = p.getName() != null ? p.getName().toLowerCase() : "";
                if (name.contains(normalizedQuery)) {
                    return 0;
                }
                return Search.distance(normalizedQuery, name);
            })
        )
        .limit(10)
        .collect(Collectors.toList());
    }

    public void prepopulateProducts() {
        try {
            var resource = new ClassPathResource("products.json");
            List<Product> products = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
            productRepository.clear();
            productRepository.saveAll(products);
        } catch (IOException e) {
            logger.error("Error loading products.json", e);
            throw new RuntimeException("Error loading products.json", e);
        }
    }
}
