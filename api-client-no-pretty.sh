#!/bin/bash

# Step 1: Generate the JWT Token
echo "Generating JWT token..."
TOKEN=$(curl -s -X POST "http://localhost:8080/api/generate-token?subject=testuser" \
       -u test-client:test-secret)

if [ -z "$TOKEN" ]; then
    echo "Error: Could not generate JWT."
    exit 1
fi

echo "Generated Token: $TOKEN"
echo ""

# Step 2: Enter interactive mode
while true; do
    echo ""
    echo "Which endpoint would you like to call?"
    echo "1) POST /api/products/prepopulate"
    echo "2) GET /api/products"
    echo "3) GET /api/search"
    echo "4) GET /api/products/{id}"
    echo "5) POST /api/products (add new product)"
    echo "6) Quit"
    read -p "Enter the number of the endpoint to call: " choice

    case $choice in
        1)
            echo "Calling: POST /api/products/prepopulate"
            curl -s -X POST "http://localhost:8080/api/products/prepopulate" \
                 -H "Authorization: Bearer $TOKEN"
            ;;
        2)
            read -p "Enter number of products to return (default: 10): " size
            size=${size:-10}  # fallback to 10 if user hits Enter
            echo "Calling: GET /api/products?size=$size"
            curl -s -X GET "http://localhost:8080/api/products?size=$size" \
                -H "Authorization: Bearer $TOKEN"
            ;;
        3)
            read -p "Enter search query: " query
            echo "Calling: GET /api/search?q=$query"
            curl -s -X GET "http://localhost:8080/api/search?q=$query" \
                 -H "Authorization: Bearer $TOKEN"
            ;;
        4)
            read -p "Enter product ID: " productId
            echo "Calling: GET /api/products/$productId"
            curl -s -X GET "http://localhost:8080/api/products/$productId" \
                 -H "Authorization: Bearer $TOKEN"
            ;;
        5)
            echo "Calling: POST /api/products"
            read -p "Enter product name: " name
            read -p "Enter product category: " category
            read -p "Enter product description: " description
            read -p "Enter product price: " price
            read -p "Enter product image URL: " imageUrl
            curl -s -X POST "http://localhost:8080/api/products" \
                 -H "Authorization: Bearer $TOKEN" \
                 -H "Content-Type: application/json" \
                 -d '{
                       "name": "'"$name"'",
                       "category": "'"$category"'",
                       "description": "'"$description"'",
                       "price": '"$price"',
                       "imageUrl": "'"$imageUrl"'"
                     }'
            ;;
        6)
            echo "Goodbye!"
            break
            ;;
        *)
            echo "Invalid choice. Please try again."
            ;;
    esac
done
