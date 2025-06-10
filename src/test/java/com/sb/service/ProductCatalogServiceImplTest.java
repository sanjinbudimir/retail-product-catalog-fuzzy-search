package com.sb.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sb.domain.Product;
import com.sb.repository.ProductCatalogRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductCatalogServiceImplTest {

    @Mock
    private ProductCatalogRepositoryInterface productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductCatalogServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("addProduct should throw exception for null product")
    void testAddProduct_NullProduct() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> productService.addProduct(null));
        assertEquals("Product cannot be null", ex.getMessage());
    }

    @Test
    @DisplayName("addProduct should throw exception for empty product name")
    void testAddProduct_EmptyName() {
        Product product = new Product();
        product.setName(" ");
        product.setCategory("Category");
        product.setPrice(10.0);
        product.setImageUrl("http://image");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> productService.addProduct(product));
        assertEquals("Product name cannot be null or empty", ex.getMessage());
    }

    @Test
    @DisplayName("addProduct should save valid product")
    void testAddProduct_ValidProduct() {
        Product product = new Product();
        product.setName("Laptop");
        product.setCategory("Electronics");
        product.setPrice(1000.0);
        product.setImageUrl("http://image");

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.addProduct(product);
        assertNotNull(saved);
        assertEquals("Laptop", saved.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    @DisplayName("getProducts should return paginated products")
    void testGetProducts_Pagination() {
        List<Product> allProducts = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            Product p = new Product();
            p.setId(String.valueOf(i));
            p.setName("Product " + i);
            allProducts.add(p);
        }

        when(productRepository.findAll()).thenReturn(allProducts);

        List<Product> page1 = productService.getProducts(0, 10);
        assertEquals(10, page1.size());
        assertEquals("Product 1", page1.get(0).getName());

        List<Product> page3 = productService.getProducts(2, 10);
        assertEquals(5, page3.size());
        assertEquals("Product 21", page3.get(0).getName());

        List<Product> pageOutOfBounds = productService.getProducts(5, 10);
        assertTrue(pageOutOfBounds.isEmpty());
    }

    @Test
    @DisplayName("getProductById should return product or throw exception")
    void testGetProductById() {
        Product product = new Product();
        product.setId("123");
        when(productRepository.findById("123")).thenReturn(Optional.of(product));

        Product found = productService.getProductById("123");
        assertNotNull(found);

        when(productRepository.findById("notfound")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.getProductById("notfound"));
        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    @DisplayName("searchProducts should throw exception for null or empty query")
    void testSearchProducts_NullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> productService.searchProducts(null));
        assertThrows(IllegalArgumentException.class, () -> productService.searchProducts("  "));
    }

    @Test
    @DisplayName("searchProducts should return matching products sorted by distance")
    void testSearchProducts_Valid() {
        Product p1 = new Product();
        p1.setName("Laptop");

        Product p2 = new Product();
        p2.setName("Smartphone");

        List<Product> allProducts = List.of(p1, p2);
        when(productRepository.findAll()).thenReturn(allProducts);

        List<Product> results = productService.searchProducts("laptop");
        assertFalse(results.isEmpty());
        assertEquals("Laptop", results.get(0).getName());
    }

    @Test
    @DisplayName("prepopulateProducts should load products from JSON and save them")
    void testPrepopulateProducts() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        ClassPathResource resource = mock(ClassPathResource.class);
        List<Product> products = List.of(new Product());

        when(objectMapper.readValue(
            any(InputStream.class),
            ArgumentMatchers.<TypeReference<List<Product>>>any()))
            .thenReturn(products);

        doNothing().when(productRepository).clear();
        doNothing().when(productRepository).saveAll(products);

        productService.prepopulateProducts();

        verify(productRepository).clear();
        verify(productRepository).saveAll(products);
    }

    @Test
    @DisplayName("prepopulateProducts should throw exception on IOException")
    void testPrepopulateProducts_Exception() throws IOException {

        when(objectMapper.readValue(any(InputStream.class), ArgumentMatchers.<TypeReference<List<Product>>>any()))
            .thenThrow(new IOException("Test exception"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.prepopulateProducts());
        assertEquals("Error loading products.json", ex.getMessage());
    }
}
