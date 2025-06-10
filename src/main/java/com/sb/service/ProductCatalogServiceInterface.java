package com.sb.service;


import java.util.List;

import com.sb.domain.Product;

public interface ProductCatalogServiceInterface {
    Product addProduct(Product product);
    List<Product> getProducts(int page, int size);
    Product getProductById(String id);
    List<Product> searchProducts(String query);
    void prepopulateProducts();
}
