package com.example.webmuasam.service;

import com.example.webmuasam.exception.BusinessException;
import com.example.webmuasam.util.EncodingUtil;
import com.example.webmuasam.util.constant.DefaultValue;
import com.example.webmuasam.util.constant.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class CryptoService {

    @Value("${payment.vnpay.secret-key}")
    private String secretKey;

    public CryptoService() {
        // Không khởi tạo Mac ở đây nữa
    }

    public String sign(String data) {
        try {
            // Mỗi lần ký phải khởi tạo Mac mới
            Mac mac = Mac.getInstance(DefaultValue.HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), DefaultValue.HMAC_SHA512);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return EncodingUtil.toHexString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Lỗi khi tạo chữ ký VNPAY", e);
            throw new BusinessException(ResponseCode.VNPAY_SIGNING_FAILED);
        }
    }
}
