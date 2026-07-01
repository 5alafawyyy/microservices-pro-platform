package com.microservices.pro.apigateway.filter;

import com.microservices.pro.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import org.springframework.util.AntPathMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * JwtAuthFilter — Session 3, "Custom Filters & JWT Validation".
 *
 * Implement the TODOs below. See docs/labs/session-03-lab-2b.md for the
 * full lab instructions and acceptance criteria.
 */
@org.springframework.stereotype.Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();    
    private record PublicRoute(HttpMethod method, String pathPattern) {}

    // TODO 1: Define PUBLIC_ROUTES list (at minimum: GET "/api/v1/products/**" — GET all and get by Id is public)
    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(
        new PublicRoute(HttpMethod.GET, "/api/v1/products/**")
    );

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // TODO 2: Implement filter() method:
        //   - Skip validation for public routes
        //   - Extract the Authorization header, expect "Bearer <token>"
        //   - Return 401 if missing or invalid
        //   - Validate the JWT via jwtUtil
        //   - Add X-User-Id and X-User-Role headers from the JWT claims
        //   - Forward the enriched request to the chain
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        boolean isPublic = PUBLIC_ROUTES.stream().anyMatch(route -> 
            route.method().equals(method) && pathMatcher.match(route.pathPattern(), path)
        );

        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        var claims = jwtUtil.validateToken(token);
        String userId = claims.getSubject();
        String role = claims.get("role", String.class);

        ServerWebExchange mutatedExchange = exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build())
            .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        // TODO 3: Return Ordered.HIGHEST_PRECEDENCE + 1
        //         (must run AFTER LoggingFilter, which is HIGHEST_PRECEDENCE)
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
