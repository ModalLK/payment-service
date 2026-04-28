package com.ctse.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "productClient", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    class ProductDto {
        private Long id;
        private String sku;
        private String name;
        private BigDecimal price;
        private Integer stock;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}

// Hello world