package com.sb.api;

import com.sb.domain.Product;
import com.sb.service.ProductCatalogServiceInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductCatalogApi.class)
class ProductCatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogServiceInterface productCatalogService;

    @MockitoBean
    private JwtDecoder jwtDecoder; 

    @Test
    @DisplayName("POST /api/products should add a valid product and return 200 OK")
    @WithMockUser
    void testAddProduct() throws Exception {
        Product product = new Product();
        product.setId("1");
        product.setName("Test Product");
        product.setCategory("Test Category");
        product.setDescription("Test Description");
        product.setPrice(10.0);
        product.setImageUrl("http://example.com");

        when(productCatalogService.addProduct(any(Product.class))).thenReturn(product);

        String productJson = """
        {
        "name": "Test Product",
        "category": "Test Category",
        "description": "Test Description",
        "price": 10.0,
        "imageUrl": "http://example.com"
        }
        """;

        mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content(productJson)
            .with(SecurityMockMvcRequestPostProcessors.jwt()))  // simulate valid JWT
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/products should return a list of products")
    @WithMockUser
    void testGetAllProducts() throws Exception {
        Product product = new Product();
        product.setId("1");
        product.setName("Test Product");
        when(productCatalogService.getProducts(eq(0), eq(10))).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products?page=0&size=10")
            .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    @DisplayName("GET /api/products/{id} should return a product")
    @WithMockUser
    void testGetProductById() throws Exception {
        Product product = new Product();
        product.setId("1");
        product.setName("Test Product");

        when(productCatalogService.getProductById("1")).thenReturn(product);

        mockMvc.perform(get("/api/products/1")
            .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"))
            .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("GET /api/search should return search results")
    @WithMockUser
    void testSearchProducts() throws Exception {
        Product product = new Product();
        product.setId("1");
        product.setName("Test Product");

        when(productCatalogService.searchProducts("test")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/search?q=test")
            .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    @DisplayName("POST /api/products/prepopulate should reload products")
    @WithMockUser
    void testPrepopulateProducts() throws Exception {
        mockMvc.perform(post("/api/products/prepopulate")
            .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isOk())
            .andExpect(content().string("Product data reloaded from JSON."));
    }
}
