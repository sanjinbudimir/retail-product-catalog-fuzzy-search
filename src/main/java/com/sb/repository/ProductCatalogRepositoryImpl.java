package com.sb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.sb.domain.Product;

@Repository
public class ProductCatalogRepositoryImpl implements ProductCatalogRepositoryInterface {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public List<Product> findAll() { return new ArrayList<>(products.values()); }

    public Optional<Product> findById(final String id) { return Optional.ofNullable(products.get(id)); }

    public Product save(final Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        product.setId(UUID.randomUUID().toString());
        products.put(product.getId(), product);
        return product;
    }

    public void clear() { products.clear(); }

    public void saveAll(final List<Product> productList) {
        for (final Product product : productList) {
            product.setId(UUID.randomUUID().toString());
            products.put(product.getId(), product);
        }
    }
}
