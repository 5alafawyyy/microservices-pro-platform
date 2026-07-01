package com.microservices.pro.orderservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final PaymentClient paymentClient;

    public OrderService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService") // Retry wraps INSIDE CircuitBreaker
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Attempting to process payment for order...");
        PaymentResponse payment = paymentClient.processPayment(new PaymentRequest(request.getAmount()));
        return new OrderResponse("CONFIRMED", payment.transactionId());
    }

    // Fallback: MUST match original params + Throwable as last param
    public OrderResponse paymentFallback(OrderRequest request, Throwable ex) {
        log.warn("Payment failed, returning PENDING. Reason: {}", ex.getMessage());
        return new OrderResponse("PENDING", "Will retry payment later");
    }
}

class OrderRequest {
    private Double amount;
    public OrderRequest() {}
    public OrderRequest(Double amount) { this.amount = amount; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}

class OrderResponse {
    private String status;
    private String transactionId;
    public OrderResponse() {}
    public OrderResponse(String status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
