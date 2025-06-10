# Retail Product Catalog API

This is a Spring Boot REST API for managing a retail product catalog, including features like adding, retrieving, and searching products with fuzzy search support. The API uses OAuth2 Resource Server security with JWT tokens and client credentials authentication.

---

## 🚀 Features

- Add new products  
- Retrieve all products with pagination  
- Search products by name (case-insensitive fuzzy search)  
- Get details of a specific product by ID  
- Prepopulate product data from JSON  
- Secure endpoints using OAuth2 with JWT tokens and client/secret authentication

- Automatically generates a JWT token using Basic Auth
- Presents a menu for calling different API endpoints
- Handles interactive prompts for parameters
- Uses jq (optional) for pretty-printing JSON output

Requirements:
curl
jq (recommended for pretty output, install with package manager, https://jqlang.org/download/, if you don't want to install run api-client-no-pretty.sh instead)


---

## 🔒 Security

- All endpoints except `/api/generate-token` and `/api/health` require a valid JWT Bearer token.
- Client credentials (configured via `application.yml`) must be supplied using Basic Auth for token generation and endpoint calls.

Example configuration:
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret-key: testtesttesttesttesttesttesttest

custom:
  client:
    id: test-client
    secret: test-secret

```


## Sample Data
````json

[
  {"name": "Laptop", "category": "Electronics", "description": "High-performance laptop", "price": 1500.0, "imageUrl": "laptop.jpg"},
  {"name": "Smartphone", "category": "Electronics", "description": "Latest model smartphone", "price": 999.0, "imageUrl": "smartphone.jpg"}
  ...
]
``````

## Build the application
```bash
mvn clean install
mvn spring-boot:run
```

## Endpoints
| Endpoint                         | Description                                |
| -------------------------------- | ------------------------------------------ |
| `POST /api/generate-token`       | Generates a JWT token for testing          |
| `GET /api/health`                | Health check endpoint                      |
| `POST /api/products`             | Add a new product                          |
| `GET /api/products`              | List products with pagination              |
| `GET /api/products/{id}`         | Get product by ID                          |
| `GET /api/search?q={query}`      | Fuzzy search products by name              |
| `POST /api/products/prepopulate` | Preload products from `products.json` file |


## Run the api-client script for testing the backend
````bash

chmod +x api-client.sh
./api-client.sh
``````


