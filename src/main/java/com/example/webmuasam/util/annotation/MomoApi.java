package com.example.webmuasam.util.annotation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.webmuasam.dto.Request.CreateMomoRequest;
import com.example.webmuasam.dto.Response.CreateMomoRespone;

@FeignClient(name = "momo", url = "${momo.end-point}")
public interface MomoApi {
    @PostMapping("/create")
    CreateMomoRespone createMomoQR(@RequestBody CreateMomoRequest createMomoRequest);
}
