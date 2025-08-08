package com.example.webmuasam.service;

import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.User;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CartItemRepository;
import com.example.webmuasam.repository.CartRepository;
import com.example.webmuasam.repository.UserRepository;
import com.example.webmuasam.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    public CartService(CartRepository cartRepository, UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart createCart(Long userId) throws AppException {
        if (userId == null) {
            throw new AppException("User ID không được null");
        }

        return this.cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = null;
            try {
                user = this.userRepository.findById(userId)
                        .orElseThrow(() -> new AppException("User not found"));
            } catch (AppException e) {
                throw new RuntimeException(e);
            }

            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }


    public Cart getCartByCurrentUser()throws Exception {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(()->new AppException("chưa đăng nhập"));

        User user = this.userRepository.findByEmail(email).orElseThrow(()->new AppException("người dùng không tồn tại"));
        return createCart(user.getId());
    }

    public void clearCart(Long cartId)throws AppException {
        Cart cart = this.cartRepository.findById(cartId).orElseThrow(()->new AppException("cart id không tồn tại"));
        this.cartItemRepository.deleteAll(cart.getCartItems());
    }



    @Transactional
    public void clearCartByUserId(Long userId) throws AppException{
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));
        cartItemRepository.deleteByCartId(cart.getId());
    }



}
