package com.example.webmuasam.dto.Response;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    double price;
    Double totalStar;
    Long quantityReview;
    String description;
    long quantity;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    List<CategoryPro> categories;
    List<String> images;
    List<Variants> variants;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Variants {
        Long id;
        String color;
        String size;
        int stockQuantity;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CategoryPro {
        Long id;
        String name;
    }
}
