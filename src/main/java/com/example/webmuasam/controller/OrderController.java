package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.OrderRequest;
import com.example.webmuasam.dto.Request.OrderRequestByCash;
import com.example.webmuasam.dto.Response.OrderResponse;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.Order;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.service.OrderService;
import com.example.webmuasam.util.RequestUtil;
import com.example.webmuasam.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {


    private final OrderService orderService;
    private final UserRepository UserRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest, HttpServletRequest httpServletRequest)throws AppException {
        var ipAddress = RequestUtil.getIpAddress(httpServletRequest);
        orderRequest.setIpAddress(ipAddress);
        log.info("[VNPay] IP Address: {}", orderRequest.getIpAddress());

        return ResponseEntity.ok().body(this.orderService.checkoutWithVnPay(orderRequest));
    }
    @PostMapping("/cash")
    public ResponseEntity<Order> createOrderByCash(@Valid @RequestBody OrderRequestByCash orderRequest)throws AppException {
        return ResponseEntity.ok().body(this.orderService.createOrderByCash(orderRequest));
    }
    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Không xác thực được người dùng"));
        User user = this.UserRepository.findByEmail(email).orElseThrow(()->new AppException("user not found"));
        orderService.cancelOrder(user.getId(), orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<ResultPaginationDTO> getOrdersByCurrentUser(Pageable pageable) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Không xác thực được người dùng"));
        User user = this.UserRepository.findByEmail(email).orElseThrow(()->new AppException("user not found"));
        ResultPaginationDTO response = orderService.getAllOrderByUser(user.getId(), pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}/status")
    public ResponseEntity<Order> getOrdersByIdStatus(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok().body(this.orderService.getOrderStatus(id));
    }
}
