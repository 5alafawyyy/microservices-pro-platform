# Microservices Professional Platform - Run & Test Guide

Congratulations! You have fully implemented all the core labs for **Sessions 1, 2, and 3**. Here is a comprehensive guide on how to spin up the entire system, test it manually, and understand what happens behind the scenes.

## 1. Start the System 🚀

You have 5 components (4 Java apps + 1 Database) that need to be running. You can run them in separate terminal windows so you can see their logs.

### Step 1: Start PostgreSQL
The `product-service` will use this database once you transition to JPA (Session 1 homework). Let's keep it running to make sure the environment is healthy.
```bash
docker compose up -d
```

### Step 2: Start Config Server (Port 8888)
*This must start first because other services fetch their configuration from it.*
```bash
cd infrastructure/config-server
mvn spring-boot:run
```

### Step 3: Start Eureka Server (Port 8761)
*This is the service registry (the phonebook).*
```bash
cd infrastructure/eureka-server
mvn spring-boot:run
```
👉 *Open [http://localhost:8761](http://localhost:8761) in your browser. You should see the Eureka dashboard.*

### Step 4: Start Product Service (Port 8081)
*This is the actual backend holding your product data.*
```bash
cd services/product-service
mvn spring-boot:run
```
👉 *Refresh the Eureka dashboard. You should now see `PRODUCT-SERVICE` registered.*

### Step 5: Start API Gateway (Port 8080)
*This is the main entry point protected by JWT and Rate Limiting.*
```bash
cd infrastructure/api-gateway
mvn spring-boot:run
```
👉 *Refresh the Eureka dashboard again. You should now see both `PRODUCT-SERVICE` and `API-GATEWAY`.*

---

## 2. Test the Implementation 🧪

### Test A: The Public Route (Session 1 & 3)
We configured `GET /api/v1/products/**` to be a **Public Route**. This means you don't need a JWT token to read products.

**Open a new terminal and run:**
```bash
curl -v http://localhost:8080/api/v1/products
```
**What happens behind the scenes?**
1. The request hits the API Gateway (`localhost:8080`).
2. Our `LoggingFilter` logs `[GATEWAY] GET /api/v1/products from 127.0.0.1`.
3. Our `JwtAuthFilter` sees this is a public route and bypasses security.
4. The Gateway looks up `PRODUCT-SERVICE` in Eureka and forwards the request to it (`localhost:8081`).
5. `ProductService` returns an empty list `[]`.
6. The Gateway forwards the response back to you with the `X-Platform: microservices-pro` header!

### Test B: The Protected Route (Session 3)
We want to add a product using a `POST` request. This is **NOT** a public route, so the API Gateway requires a valid JWT Token.

**1. Try without a token (Expect 401 Unauthorized):**
```bash
curl -v -X POST http://localhost:8080/api/v1/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Laptop", "price":999.99}'
```
*You will get an HTTP `401 Unauthorized` because you didn't provide a Bearer token!*

**2. Try with a valid token:**
You need a generated token. If your instructor provided a `tools/jwt-generator`, you can generate one. Assuming you have a token `$MY_TOKEN`:
```bash
curl -v -X POST http://localhost:8080/api/v1/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $MY_TOKEN" \
     -d '{"name":"Laptop", "description":"15-inch", "price":999.99, "category":"Electronics"}'
```
**What happens behind the scenes?**
1. `JwtAuthFilter` extracts your token, validates the signature using `jwt.secret`, and extracts your `userId`.
2. It mutates the request to include `X-User-Id: <your-id>`.
3. The Rate Limiter allows the request to pass.
4. The Product Service receives the request, generates `ID=1`, and saves the laptop to its `ConcurrentHashMap`.

### Test C: Verify the Product was saved
Now retrieve the products again using the public route:
```bash
curl http://localhost:8080/api/v1/products
```
You should see your newly created Laptop!

---

## 3. Session Documentation Summary 📚

- **Session 1 (Product Service + Foundations)**: You built a Spring Boot backend (`ProductService`) that uses a thread-safe `ConcurrentHashMap` to store products in memory. You also spun up a Config Server and Eureka so the services can configure and discover each other.
- **Session 2 (API Gateway + Routing)**: You built a Spring Cloud Gateway (`API Gateway`). You configured it to automatically route `/api/v1/products` requests to the `PRODUCT-SERVICE` using Eureka (`lb://PRODUCT-SERVICE`). You also added a global `LoggingFilter` to log all incoming traffic.
- **Session 3 (Security + Rate Limiting)**: You hardened the API Gateway. You added `JwtAuthFilter` to enforce that non-public routes require a valid JWT token. You also prepared the `RateLimitConfig` with a `KeyResolver` to protect against spam attacks based on User ID or IP Address.
