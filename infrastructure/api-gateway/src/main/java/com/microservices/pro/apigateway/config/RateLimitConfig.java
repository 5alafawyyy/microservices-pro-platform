package com.microservices.pro.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * RateLimitConfig — Session 3, "Rate Limiting with Redis".
 *
 * Implement the TODOs below. See docs/labs/session-03-lab-2b.md.
 */
@Configuration
public class RateLimitConfig {

    // TODO 1: Create an IP-based KeyResolver bean, annotated @Bean and @Primary
    //         Use exchange.getRequest().getRemoteAddress()
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown"
        );
    }

    // TODO 2 (BONUS): Create a user-based KeyResolver bean
    //         Use the X-User-Id header, defaulting to "anonymous"
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

}
