package com.microservices.pro.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * LoggingFilter — Session 2, Lab 2A, Task 2.
 *
 * GlobalFilter applied to ALL routes (not route-specific). Implement the
 * TODOs below. See docs/labs/session-02-lab-02.md for the full lab
 * instructions and acceptance criteria.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    // TODO 1: Inject a Logger (SLF4J)

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 2: Implement filter() method
        //         - Log: method + path + remote address (pre-filter)
        //         - Log: response status code (post-filter)
        //         - Chain the filter correctly
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // TODO 3: Return Ordered.HIGHEST_PRECEDENCE
        return 0;
    }
}
