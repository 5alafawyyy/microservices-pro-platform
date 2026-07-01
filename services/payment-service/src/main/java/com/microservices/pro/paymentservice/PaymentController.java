package com.microservices.pro.paymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final Random random = new Random();

    @Value("${payment.failure.rate:50}")
    private int failureRate;

    @PostMapping
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment for amount: {}", request.amount());

        // Simulate failure based on configured rate (0 to 100)
        if (random.nextInt(100) < failureRate) {
            log.error("Simulated payment failure triggered!");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Payment Service unavailable");
        }

        String transactionId = UUID.randomUUID().toString();
        log.info("Payment approved. Transaction ID: {}", transactionId);
        return new PaymentResponse("APPROVED", transactionId);
    }
}

record PaymentRequest(Double amount) {}
record PaymentResponse(String status, String transactionId) {}
