package com.example.webmuasam.service;

import com.example.webmuasam.dto.Request.InitPaymentRequest;
import com.example.webmuasam.dto.Response.InitPaymentResponse;

public interface  PaymentService {
    InitPaymentResponse init(InitPaymentRequest request);

}
