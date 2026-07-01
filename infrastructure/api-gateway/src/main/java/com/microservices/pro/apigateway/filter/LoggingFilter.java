package com.microservices.pro.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 2: Implement filter() method
        //         - Log: method + path + remote address (pre-filter)
        //         - Log: response status code (post-filter)
        //         - Chain the filter correctly
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
                
        logger.info("[GATEWAY] {} {} from {}", method, path, remoteAddress);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("[GATEWAY] Response status: {}", exchange.getResponse().getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        // TODO 3: Return Ordered.HIGHEST_PRECEDENCE
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
