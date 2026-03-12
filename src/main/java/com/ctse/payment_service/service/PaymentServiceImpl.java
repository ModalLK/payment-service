package com.ctse.payment_service.service;

import com.ctse.payment_service.client.ProductClient;
import com.ctse.payment_service.dto.PaymentCheckoutRequest;
import com.ctse.payment_service.dto.PaymentResponse;
import com.ctse.payment_service.entity.Payment;
import com.ctse.payment_service.entity.PaymentItem;
import com.ctse.payment_service.entity.PaymentStatus;
import com.ctse.payment_service.exception.InsufficientStockException;
import com.ctse.payment_service.exception.ResourceNotFoundException;
import com.ctse.payment_service.repo.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProductClient productClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, ProductClient productClient) {
        this.paymentRepository = paymentRepository;
        this.productClient = productClient;
    }

    @Override
    @Transactional
    public PaymentResponse checkout(PaymentCheckoutRequest req) {
        if (req.getCart() == null || req.getCart().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Payment payment = new Payment();
        payment.setCustomerName(req.getCustomer().getFullName());
        payment.setCustomerPhone(req.getCustomer().getPhone());
        payment.setCustomerAddress(req.getCustomer().getAddress() + ", " + req.getCustomer().getCity());
        payment.setPaymentMethod(req.getPayment().getMethod());
        payment.setStatus(PaymentStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (PaymentCheckoutRequest.CartItem cartItem : req.getCart()) {
            ProductClient.ProductDto product = productClient.getProduct(cartItem.getProductId());

            if (product == null || product.getId() == null) {
                throw new ResourceNotFoundException("Product not found: " + cartItem.getProductId());
            }

            if (product.getStock() == null || product.getStock() < cartItem.getQty()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQty()));

            PaymentItem item = new PaymentItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setUnitPrice(unitPrice);
            item.setQuantity(cartItem.getQty());
            item.setLineTotal(lineTotal);

            payment.addItem(item);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal shipping = subtotal.compareTo(new BigDecimal("5000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("350");

        BigDecimal total = subtotal.add(shipping);

        payment.setSubtotal(subtotal);
        payment.setShipping(shipping);
        payment.setAmount(total);

        boolean gatewaySuccess = simulateGateway(req.getPayment().getMethod());

        if (gatewaySuccess) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setGatewayTransactionId("TXN-" + UUID.randomUUID());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Mock payment gateway declined the transaction");
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    @Override
    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return mapToResponse(payment);
    }

    @Override
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found: " + id);
        }
        paymentRepository.deleteById(id);
    }

    private boolean simulateGateway(String method) {
        if (method == null) return false;
        return !"FAIL".equalsIgnoreCase(method);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setCustomerName(payment.getCustomerName());
        response.setSubtotal(payment.getSubtotal());
        response.setShipping(payment.getShipping());
        response.setTotal(payment.getAmount());
        response.setStatus(payment.getStatus().name());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setGatewayTransactionId(payment.getGatewayTransactionId());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());

        List<PaymentResponse.PaymentItemResponse> items = payment.getItems().stream().map(item -> {
            PaymentResponse.PaymentItemResponse dto = new PaymentResponse.PaymentItemResponse();
            dto.setProductId(item.getProductId());
            dto.setProductName(item.getProductName());
            dto.setProductSku(item.getProductSku());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setQuantity(item.getQuantity());
            dto.setLineTotal(item.getLineTotal());
            return dto;
        }).collect(Collectors.toList());

        response.setItems(items);
        return response;
    }
}