package com.microservices.pro.orderservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_paymentSucceeds_returnsConfirmed() {
        when(paymentClient.processPayment(any(PaymentRequest.class)))
                .thenReturn(new PaymentResponse("APPROVED", "txn-123"));

        OrderResponse response = orderService.createOrder(new OrderRequest(100.0));

        assertEquals("CONFIRMED", response.getStatus());
        assertEquals("txn-123", response.getTransactionId());
        verify(paymentClient, times(1)).processPayment(any(PaymentRequest.class));
    }

    @Test
    void paymentFallback_returnsPending() {
        // Direct test of the fallback method
        OrderResponse response = orderService.paymentFallback(new OrderRequest(100.0), 
            new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        assertEquals("PENDING", response.getStatus());
        assertEquals("Will retry payment later", response.getTransactionId());
    }
}
