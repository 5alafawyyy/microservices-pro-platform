# Session 4 Implementation Walkthrough: Circuit Breakers & Retry

We have successfully implemented **Lab 3A** based on the Session 4 slides. This lab introduced the `payment-service` and `order-service` to demonstrate what happens when a downstream dependency fails and how to protect the system using Resilience4j.

## What was built

1. **Payment Service (Port 8083)**:
   - A new microservice designed with a simulated `failure.rate` config property (currently set to 50%).
   - Randomly returns a `200 APPROVED` or throws a `503 Service Unavailable` to simulate network errors or timeouts during high load.
   - [payment-service code](file:///E:/Java%20Spring/Microservices%20Training/TIMELINES/Shared_Repo/microservices-pro-platform/services/payment-service/src/main/java/com/microservices/pro/paymentservice/PaymentController.java)

2. **Order Service (Port 8082)**:
   - A new microservice responsible for taking orders and communicating with the Payment Service via a `@LoadBalanced RestTemplate`.
   - **Retry Mechanism**: Wrapped the `processPayment` call with a `@Retry` annotation that attempts the call up to 3 times, with an exponential backoff starting at 500ms and doubling each time (500ms → 1s → 2s).
   - **Circuit Breaker**: Protected the entire call with a `@CircuitBreaker`. If 50% of the last 10 requests fail, the circuit opens, rejecting all traffic immediately to prevent cascading failures.
   - **Fallback Method**: If the `Payment Service` fails consistently, instead of throwing an error to the user, the `Order Service` catches the exception and returns a graceful `PENDING` response, telling the user their order will be retried later.
   - [order-service code](file:///E:/Java%20Spring/Microservices%20Training/TIMELINES/Shared_Repo/microservices-pro-platform/services/order-service/src/main/java/com/microservices/pro/orderservice/OrderService.java)

## Verification
- We wrote robust unit tests mocking the Payment Service and validating the success flow as well as the exception handling fallback flow.
- We ran `mvn test` in the `order-service` and all tests passed successfully!

> [!TIP]
> **Try it yourself!** Run both services locally alongside Eureka. Send multiple `POST /api/orders` requests. Watch your terminal logs in `order-service` — you will see the `RetryLogger` printing `[RETRY] Attempt #1`, `#2`, and eventually falling back to a `PENDING` order when the mock failures strike too many times!
