package com.ctse.payment_service.controller;

import com.ctse.payment_service.dto.PaymentCheckoutRequest;
import com.ctse.payment_service.dto.PaymentResponse;
import com.ctse.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "payment-service",
                "status", "running"
        );
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponse> checkout(@Valid @RequestBody PaymentCheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.checkout(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> list() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        paymentService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
    }
}