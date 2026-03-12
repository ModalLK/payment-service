package com.ctse.payment_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public class PaymentCheckoutRequest {

    @Valid
    @NotNull
    private Customer customer;

    @Valid
    @NotNull
    private PaymentInfo payment;

    @Valid
    @NotEmpty
    private List<CartItem> cart;

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public PaymentInfo getPayment() { return payment; }
    public void setPayment(PaymentInfo payment) { this.payment = payment; }

    public List<CartItem> getCart() { return cart; }
    public void setCart(List<CartItem> cart) { this.cart = cart; }

    public static class Customer {
        @NotBlank
        private String fullName;

        @NotBlank
        private String phone;

        @NotBlank
        private String address;

        @NotBlank
        private String city;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public static class PaymentInfo {
        @NotBlank
        private String method; // CARD, COD, MOCK

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
    }

    public static class CartItem {
        @NotNull
        private Long productId;

        @NotNull
        @Positive
        private Integer qty;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQty() { return qty; }
        public void setQty(Integer qty) { this.qty = qty; }
    }
}