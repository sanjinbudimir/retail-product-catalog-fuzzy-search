package com.sb.repository;

import com.sb.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogRepositoryImplTest {

    private ProductCatalogRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ProductCatalogRepositoryImpl();
    }

    @Test
    @DisplayName("findAll should return all saved products")
    void testFindAll() {
        Product product1 = new Product();
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setName("Product 2");

        repository.save(product1);
        repository.save(product2);

        List<Product> products = repository.findAll();
        assertEquals(2, products.size());
    }

    @Test
    @DisplayName("findById should return the correct product")
    void testFindById() {
        Product product = new Product();
        product.setName("Product 1");
        Product savedProduct = repository.save(product);

        Optional<Product> found = repository.findById(savedProduct.getId());
        assertTrue(found.isPresent());
        assertEquals(savedProduct.getName(), found.get().getName());
    }

    @Test
    @DisplayName("findById should return empty for non-existent product")
    void testFindById_NonExistent() {
        Optional<Product> found = repository.findById("non-existent-id");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("save should assign a UUID and store the product")
    void testSave() {
        Product product = new Product();
        product.setName("Product 1");

        Product savedProduct = repository.save(product);

        assertNotNull(savedProduct.getId());
        assertEquals(product.getName(), savedProduct.getName());

        Optional<Product> retrieved = repository.findById(savedProduct.getId());
        assertTrue(retrieved.isPresent());
    }

    @Test
    @DisplayName("save should throw exception when product is null")
    void testSave_NullProduct() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    @DisplayName("clear should remove all products")
    void testClear() {
        Product product = new Product();
        product.setName("Product 1");
        repository.save(product);

        assertFalse(repository.findAll().isEmpty());

        repository.clear();

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("saveAll should save multiple products with assigned UUIDs")
    void testSaveAll() {
        Product product1 = new Product();
        product1.setName("Product 1");

        Product product2 = new Product();
        product2.setName("Product 2");

        repository.saveAll(List.of(product1, product2));

        List<Product> products = repository.findAll();
        assertEquals(2, products.size());
        assertTrue(products.stream().allMatch(p -> p.getId() != null));
    }
}
