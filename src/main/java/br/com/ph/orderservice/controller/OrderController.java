package br.com.ph.orderservice.controller;

import br.com.ph.orderservice.dto.OrderRequest;
import br.com.ph.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestBody OrderRequest request) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                var result = service.place(request);
                if (result) {
                    return ResponseEntity.status(HttpStatus.CREATED).body("Order placed !");
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
            });

        } catch (Exception e) {
            return CompletableFuture.supplyAsync(()-> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getLocalizedMessage()));

        }
    }

    public CompletableFuture<ResponseEntity<String>> fallbackMethod(OrderRequest request, Exception e) {
        return CompletableFuture.supplyAsync(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getLocalizedMessage()));
    }

}
