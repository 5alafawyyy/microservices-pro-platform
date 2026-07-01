# The Absolute Beginner's Guide to Microservices 🚀
*(Covering the Microservices Professional Platform - Sessions 1 to 3)*

Welcome to the world of Microservices! If you have never built a microservice before, don't worry. This guide takes you from zero to understanding the theory from the course slides, how it works, and every line of code we wrote to make it happen.

---

## 📖 Session 1: Architecture & Spring Cloud

### The Problem: Why Not Just Build One Big Application?
Traditionally, developers build a **Monolith** — one gigantic application with one codebase and one database. While simple, it has massive drawbacks:
- Any small change requires a full redeploy of the entire system.
- One bug can crash the entire system.
- To scale one heavily used feature, you have to scale EVERYTHING.

### The Solution: Microservices
Microservices is an architectural style where an application is composed of small, independently deployable services. Each service owns its own data and business capability.
- **Gain:** Independent deployments, selective scaling, team autonomy.
- **Cost:** Operational complexity, network latency, distributed debugging.

### Domain-Driven Design (Bounded Context)
How do we decide where to cut the monolith? We use **Bounded Contexts**. Services are split by business domain ownership — not by database structure.
- **Product Catalogue:** Read-heavy. Owned by Catalogue team.
- **Inventory:** Write-heavy. Owned by Fulfilment team.

### Spring Cloud Config Server (The 12-Factor App)
Factor III of the *12-Factor App* states: **"Store config in the environment"**. 
If 8 microservices need a database password, we don't want to edit 8 different files! We use a **Config Server** (running on port `8888`) that serves centralized configuration from a single Git repository to all services when they start up.

### Service Discovery (Eureka)
**Problem:** Hardcoded IPs break when instances scale or restart.
**Solution:** The **Eureka Server** (port `8761`) acts as a "Phonebook".
1. A service (like Product Service) starts up and registers its name (`PRODUCT-SERVICE`) and current IP address with Eureka.
2. Other services ask Eureka "Where is PRODUCT-SERVICE?" and get the live address back dynamically.

### Session 1 Code Implementation

**1. Eureka Server Application**
We added `@EnableEurekaServer` to turn a basic Spring Boot app into our phonebook.
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**2. Product Service (In-Memory Database)**
To build our first business service, we created an in-memory `ConcurrentHashMap` to act as our database until we add PostgreSQL later.
```java
// Inside com.microservices.pro.productservice.ProductService
private final Map<Long, Product> store = new ConcurrentHashMap<>();
private final AtomicLong idGenerator = new AtomicLong(1);

public Product save(Product product) {
    Long id = product.id();
    if (id == null) {
        id = idGenerator.getAndIncrement();
        product = new Product(id, product.name(), product.description(), product.price(), product.category());
    }
    store.put(id, product);
    return product;
}
// We also added findAll(), findById(), and deleteById() using the 'store' map.
```

---

## 🚪 Session 2: API Gateway & Routing

### The Problem: Without a Gateway
If a mobile app needs to fetch Products, Orders, and Payments, it has to know the exact IP address of every single microservice. There is no single place to add security, and clients must update every time a service changes location.

### The Solution: API Gateway
The API Gateway (port `8080`) is the **Single Entry Point**. Clients only ever know about the Gateway. Everything behind it is invisible.
The Gateway has six responsibilities:
1. **Routing:** Matching requests to services.
2. **Load Balancing:** Distributing traffic across instances.
3. **Authentication:** Validating who is calling.
4. **Rate Limiting:** Protecting services from spam.
5. **Observability:** Logging traffic.
6. **Request Shaping:** Modifying headers.

### Route Predicates ("The IF")
The Gateway uses "Predicates" to decide where a request goes.
- **Path:** `Path=/api/products/**` routes to the Product Service.

### Filters ("The THEN")
Filters happen after a route matches.
- **GatewayFilter:** Applies to ONE specific route (e.g., `StripPrefix=0`).
- **GlobalFilter:** Applies to ALL routes (e.g., Logging, CORS).

### Session 2 Code Implementation

**1. API Gateway Configuration (`application.yml`)**
We told the Gateway to look at the path, and use Eureka (`lb://`) to load balance the traffic to the `PRODUCT-SERVICE`.
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/v1/products/**
          filters:
            - StripPrefix=0
            - AddResponseHeader=X-Platform, microservices-pro
```

**2. Global Logging Filter**
We implemented a `GlobalFilter` to act as a security camera, logging every request. We returned `Ordered.HIGHEST_PRECEDENCE` so it runs first!
```java
// Inside com.microservices.pro.apigateway.filter.LoggingFilter
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String method = exchange.getRequest().getMethod().name();
    String path = exchange.getRequest().getURI().getPath();
    String remoteAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            
    logger.info("[GATEWAY] {} {} from {}", method, path, remoteAddress);

    return chain.filter(exchange).then(Mono.fromRunnable(() -> {
        logger.info("[GATEWAY] Response status: {}", exchange.getResponse().getStatusCode());
    }));
}
```

---

## 🔒 Session 3: API Gateway Advanced (Security)

### The Trust Boundary
The Gateway acts as the "Trust Boundary". We don't want every microservice doing complex math to validate security tokens. Instead:
1. The Gateway validates the JWT token ONCE.
2. The Gateway extracts the user's ID and adds it to the HTTP Headers (`X-User-Id`).
3. The Gateway forwards the request. The internal microservices *trust* the Gateway and just read the header!

### Token Bucket Algorithm (Rate Limiting)
To prevent bots from crashing our Product Service with 10,000 requests a second, we use the **Token Bucket** algorithm via Redis:
- **Bucket Capacity:** Holds max 20 tokens (burst).
- **Replenish Rate:** Adds 10 tokens per second (steady rate).
- **Cost:** Each request costs 1 token. If empty, the user gets a `429 Too Many Requests` error.

### Session 3 Code Implementation

**1. JWT Utility**
We used the `io.jsonwebtoken` library and an HMAC-SHA key to validate tokens using a secret key.
```java
// Inside com.microservices.pro.apigateway.security.JwtUtil
public Claims validateToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseClaimsJws(token)
            .getBody();
}
```

**2. JWT Authentication Filter**
We created a second `GlobalFilter`. It checks if the route is public (`GET /api/v1/products/**`). If not, it expects a `Bearer` token.
```java
// Inside com.microservices.pro.apigateway.filter.JwtAuthFilter
if (isPublic) {
    return chain.filter(exchange); // Bypass security for public routes!
}

String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
String token = authHeader.substring(7); // Strip "Bearer "

// Validate using the JwtUtil
var claims = jwtUtil.validateToken(token);
String userId = claims.getSubject();
String role = claims.get("role", String.class);

// Header Enrichment: Add X-User-Id and X-User-Role to the request!
ServerWebExchange mutatedExchange = exchange.mutate()
    .request(exchange.getRequest().mutate()
        .header("X-User-Id", userId)
        .header("X-User-Role", role)
        .build())
    .build();

return chain.filter(mutatedExchange);
```

**3. Rate Limiting Keys**
We told the Gateway how to identify unique users (so one spammer doesn't block everyone else). We created a `KeyResolver` that looks at their IP Address.
```java
// Inside com.microservices.pro.apigateway.config.RateLimitConfig
@Bean
@Primary
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
        .map(addr -> addr.getAddress().getHostAddress())
        .defaultIfEmpty("unknown");
}
```

---

## 🏃 Part 4: How to Run & Test It Locally

**1. Start the System:**
Open separate terminals and run these in order:
1. `docker compose up -d` (Starts Redis for rate limiting)
2. `cd infrastructure/config-server && mvn spring-boot:run`
3. `cd infrastructure/eureka-server && mvn spring-boot:run`
4. `cd services/product-service && mvn spring-boot:run`
5. `cd infrastructure/api-gateway && mvn spring-boot:run`

**2. Test Security & Rate Limiting:**
*   **Public Read:** `curl -v http://localhost:8080/api/v1/products` (Works instantly!)
*   **Protected Write (No Token):** `curl -v -X POST http://localhost:8080/api/v1/products -d "{...}"` (Fails with `401 Unauthorized`)
*   **Protected Write (Valid Token):** `curl -v -X POST -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/products -d "{...}"` (Succeeds with `201 Created` and returns `X-RateLimit` headers!)

---

## 🛡️ Session 4: Resilience Patterns

### The Problem: Cascading Failures
Imagine Black Friday: 50,000 users place orders simultaneously. The Payment Service becomes very slow (taking 10 seconds). The Order Service uses all its threads waiting for the Payment Service. Now the Order Service is unresponsive. The Gateway then fails. One slow service brings down the **entire platform**. This is called a **Cascading Failure**.

### The Solution: Circuit Breaker & Retry (Resilience4j)
To protect our services, we use Resilience Patterns:
1. **Circuit Breaker (Fail Fast):** If 50% of the last 10 calls fail, the Circuit Breaker "OPENS" and immediately rejects new calls without even trying to hit the Payment Service. This gives the Payment Service time to recover.
2. **Fallback (Fail Safe):** When the Circuit Breaker is open, or a call fails, we provide a "Fallback" response (e.g., returning a `PENDING` order status instead of crashing).
3. **Retry with Exponential Backoff:** For temporary network glitches, we automatically retry the call. We wait 500ms, then 1000ms, then 2000ms before giving up.

### Session 4 Code Implementation

**1. Payment Service (Port 8083)**
We built a new `Payment Service` to simulate these failures. It uses a random number generator to randomly throw a `503 Service Unavailable` error based on a configurable `failure.rate`.

**2. Order Service (Port 8082)**
We created the `Order Service` which acts as the client calling the `Payment Service`.

**Circuit Breaker & Retry Configuration (`application.yml`)**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      paymentService:
        sliding-window-size: 10 # track last 10 calls
        failure-rate-threshold: 50 # open if 50%+ fail
        wait-duration-in-open-state: 5s # wait 5s before HALF-OPEN (testing recovery)
  retry:
    instances:
      paymentService:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
```

**OrderService.java**
We annotated our `createOrder` method so that every time it calls the `PaymentClient`, it is wrapped in a Retry and a Circuit Breaker.
```java
// Inside com.microservices.pro.orderservice.OrderService
@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
@Retry(name = "paymentService") // Retry wraps INSIDE CircuitBreaker
public OrderResponse createOrder(OrderRequest request) {
    PaymentResponse payment = paymentClient.processPayment(new PaymentRequest(request.getAmount()));
    return new OrderResponse("CONFIRMED", payment.transactionId());
}

// Fallback: MUST match original params + Throwable as last param
public OrderResponse paymentFallback(OrderRequest request, Throwable ex) {
    log.warn("Payment failed, returning PENDING. Reason: {}", ex.getMessage());
    return new OrderResponse("PENDING", "Will retry payment later");
}
```
*Notice how the fallback returns `PENDING`. The user is never left hanging with an ugly 500 Server Error!*
