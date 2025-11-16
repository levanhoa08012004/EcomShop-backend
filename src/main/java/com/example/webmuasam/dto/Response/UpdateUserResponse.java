package com.example.webmuasam.dto.Response;

import java.time.Instant;

import com.example.webmuasam.util.constant.GenderEnum;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class UpdateUserResponse {
    long id;
    String username;
    String email;
    String address;
    GenderEnum gender;
    String image;

    Instant updatedAt;
    String updatedBy;
    RoleUser role;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        long id;
        String name;
    }
}
