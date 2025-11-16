package com.example.webmuasam.dto.Response;

import com.example.webmuasam.util.constant.StatusOrder;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseByCash {
    Long id;
    double total_price;
    StatusOrder status;
    String fullName;
    String phoneNumber;
    String email;
    String address;
}
