package com.example.webmuasam.dto.Response;

import com.example.webmuasam.util.constant.GenderEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetAllUserResponse {
    long id;
    String username;
    String email;
    String address;
    GenderEnum gender;
    String image;

    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    RoleUser role;


    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser{
        long id;
        String name;
    }

}
