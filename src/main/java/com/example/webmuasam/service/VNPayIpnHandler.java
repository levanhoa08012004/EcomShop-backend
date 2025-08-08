package com.example.webmuasam.service;

import com.example.webmuasam.dto.Response.IpnResponse;
import com.example.webmuasam.exception.BusinessException;
import com.example.webmuasam.util.constant.VNPayParams;
import com.example.webmuasam.util.constant.VnpIpnResponseConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VNPayIpnHandler implements IpnHandler {

    private final VNPayService vnPayService;

    private final OrderService orderService;


    public IpnResponse process(Map<String, String> params) {
        if (!vnPayService.verifyIpn(params)) {
            return VnpIpnResponseConst.SIGNATURE_FAILED;
        }

        IpnResponse response;
        var txnRef = params.get(VNPayParams.TXN_REF);
        try {
            var orderId = Long.parseLong(txnRef);
            orderService.confirmVnPayPayment(orderId,true);
            response = VnpIpnResponseConst.SUCCESS;
        } catch (BusinessException e) {
            switch (e.getResponseCode()) {
                case ORDER_NOT_FOUND -> response = VnpIpnResponseConst.ORDER_NOT_FOUND;
                default -> response = VnpIpnResponseConst.UNKNOWN_ERROR;
            }
        } catch (Exception e) {
            response = VnpIpnResponseConst.UNKNOWN_ERROR;
        }

        log.info("[VNPay Ipn] txnRef: {}, response: {}", txnRef, response);
        return response;
    }
}