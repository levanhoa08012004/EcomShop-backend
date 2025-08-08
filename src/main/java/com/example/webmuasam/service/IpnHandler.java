package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.IpnResponse;

import java.util.Map;

public interface IpnHandler {
    IpnResponse process(Map<String, String> params);
}
