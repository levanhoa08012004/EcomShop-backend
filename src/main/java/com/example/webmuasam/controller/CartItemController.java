package com.example.webmuasam.controller;

import com.example.webmuasam.dto.Request.CreateCartItemRequest;
import com.example.webmuasam.dto.Response.CartItemResponse;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.service.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cartitems")
public class CartItemController {
    private final CartItemService cartItemService;
    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<CartItemResponse>> getAllCartItemByCartId(@PathVariable Long id) throws AppException {
        return ResponseEntity.ok(this.cartItemService.getAllCartItemByCartId(id));
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> createCartItem(@RequestBody CreateCartItemRequest request) throws AppException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.cartItemService.addCartItem(request));
    }

    @PutMapping("/incre/{id}")
    public ResponseEntity<CartItemResponse> increCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.increCartItem(id));
    }
    @PutMapping("/des/{id}")
    public ResponseEntity<CartItemResponse> desCartItem(@PathVariable Long id) throws AppException {
        return ResponseEntity.status(HttpStatus.OK).body(this.cartItemService.desCartItem(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id) throws AppException {
        this.cartItemService.deleteCartItem(id);
        return ResponseEntity.ok("delete cartItem is success");
    }

}
