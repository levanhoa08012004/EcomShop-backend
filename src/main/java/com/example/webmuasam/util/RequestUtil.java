package com.example.webmuasam.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {

    public static String getIpAddress(HttpServletRequest request) {
        // Lấy IP từ header nếu đi qua proxy hoặc ngrok
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }

        // Nếu không có header X-Forwarded-For thì lấy IP trực tiếp
        String remoteAddr = request.getRemoteAddr();

        // Nếu chạy localhost hoặc ngrok, trả về IP thật (IP Public của bạn)
        if ("127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr) || remoteAddr.startsWith("0:0:0:0:0:0:0:1")) {
            return "14.245.241.5";  // ⚠️ Thay bằng IP public của bạn nếu thay đổi mạng
        }

        return remoteAddr;
    }
}
