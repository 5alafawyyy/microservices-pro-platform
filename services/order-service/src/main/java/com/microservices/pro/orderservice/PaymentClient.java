package com.microservices.pro.orderservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentClient.class);
    private final RestTemplate restTemplate;

    public PaymentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Sending payment request to PAYMENT-SERVICE...");
        // Call the payment-service registered in Eureka
        return restTemplate.postForObject("http://PAYMENT-SERVICE/api/payments", request, PaymentResponse.class);
    }
}

record PaymentRequest(Double amount) {}
record PaymentResponse(String status, String transactionId) {}
