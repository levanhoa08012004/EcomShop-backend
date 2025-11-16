package com.example.webmuasam.dto.Response;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import com.example.webmuasam.util.constant.PaymentMethod;
import com.example.webmuasam.util.constant.StatusOrder;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponseDetail {
    Long id;
    double total_price;
    StatusOrder status;
    String fullName;
    String phoneNumber;
    String email;
    String address;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    PaymentMethod paymentMethod;
    UserOrder user;
    List<OrderDetailUser> orderDetails;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserOrder {
        Long id;
        String username;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderDetailUser {
        Long id;
        int quantity;
        double price;
        Long productId;
        ProductVariantOrder productVariant;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ProductVariantOrder {
        Long id;
        String color;
        String size;
        int stockQuantity;
        String image;
        String name;
    }
}
