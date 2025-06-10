package com.sb.repository;

import java.util.List;
import java.util.Optional;

import com.sb.domain.Product;

public interface ProductCatalogRepositoryInterface {
    List<Product> findAll();
    Optional<Product> findById(String id);
    Product save(Product product);
    void clear();
    void saveAll(List<Product> products);
}
