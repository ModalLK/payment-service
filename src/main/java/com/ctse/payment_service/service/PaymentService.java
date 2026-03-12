package com.ctse.payment_service.service;

import com.ctse.payment_service.dto.PaymentCheckoutRequest;
import com.ctse.payment_service.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse checkout(PaymentCheckoutRequest request);
    PaymentResponse getById(Long id);
    List<PaymentResponse> getAll();
    void deleteById(Long id);
}