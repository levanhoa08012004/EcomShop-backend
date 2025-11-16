package com.example.webmuasam.dto.Response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponse implements Serializable {
    private String status;
    private String message;
    private String URL;
}
