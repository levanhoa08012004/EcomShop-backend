package com.example.webmuasam.service;

import com.example.webmuasam.dto.Request.CreateCartItemRequest;
import com.example.webmuasam.dto.Response.CartItemResponse;
import com.example.webmuasam.entity.Cart;
import com.example.webmuasam.entity.CartItem;
import com.example.webmuasam.entity.Product;
import com.example.webmuasam.entity.ProductVariant;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.CartItemRepository;
import com.example.webmuasam.repository.CartRepository;
import com.example.webmuasam.repository.ProductRepository;
import com.example.webmuasam.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    public CartItemService(CartItemRepository cartItemRepository, CartRepository cartRepository, ProductVariantRepository productVariantRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productVariantRepository = productVariantRepository;
    }
    public CartItemResponse convertToResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setQuantity(cartItem.getQuantity());
        response.setPrice(cartItem.getPrice());

        ProductVariant pv = cartItem.getProductVariant();
        Product p = pv.getProduct();

        CartItemResponse.ProductInfo productInfo = new CartItemResponse.ProductInfo(p.getId(), p.getName(), p.getPrice());
        CartItemResponse.ProductVariantInfo variantInfo = new CartItemResponse.ProductVariantInfo(pv.getId(), pv.getColor(), pv.getSize(), productInfo);

        response.setProductVariant(variantInfo);

        return response;
    }


    public CartItemResponse addCartItem(CreateCartItemRequest request) throws AppException {
        Cart cart= this.cartRepository.findById(request.getCartId()).orElseThrow(()->new AppException("cart không tồn tại"));
        ProductVariant productVariant= this.productVariantRepository.findById(request.getProductVariantId()).orElseThrow(()->new AppException("product không tồn tại"));

        Optional<CartItem> optionalCartItem = this.cartItemRepository.findByCartAndProductVariant(cart, productVariant);
        CartItem cartItem;
        if(optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            int newQuantity = request.getQuantity() + cartItem.getQuantity();
            if(newQuantity > productVariant.getStockQuantity()){
                throw new AppException("Số lượng sản phẩm hiện tại không đủ, chỉ còn " +productVariant.getStockQuantity());
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(productVariant.getProduct().getPrice() * newQuantity);
        }else {

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(productVariant);
            if (request.getQuantity() > productVariant.getStockQuantity()) {
                throw new AppException("Số lượng sản phẩm hiện tại không đủ, chỉ còn " + productVariant.getStockQuantity());
            }
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(productVariant.getProduct().getPrice() * request.getQuantity());

        }
        ;
        CartItem cartItemResponse = this.cartItemRepository.save(cartItem);
        return convertToResponse(cartItemResponse);
    }

    public CartItemResponse increCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = this.cartItemRepository.findById(cartItemId).orElseThrow(()->new AppException("cartItem không tồn tại"));
        int newQuantity = cartItem.getQuantity() + 1;
        if(newQuantity > cartItem.getProductVariant().getStockQuantity()){
            throw new AppException("Số lượng sản phẩm hiện tại không đủ, chỉ còn " + cartItem.getProductVariant().getStockQuantity());
        }
        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(cartItem.getProductVariant().getProduct().getPrice() * newQuantity);
        CartItem c= this.cartItemRepository.save(cartItem);
        return convertToResponse(c);
    }

    public void deleteCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = this.cartItemRepository.findById(cartItemId).orElseThrow(()-> new AppException("cart item không tồn tại"));
        this.cartItemRepository.delete(cartItem);
    }

    public CartItemResponse desCartItem(Long cartItemId) throws AppException {
        CartItem cartItem = this.cartItemRepository.findById(cartItemId).orElseThrow(()->new AppException("cart item không tồn tại"));
        int newQuantity = cartItem.getQuantity() - 1;
        if(newQuantity <= 0){
            deleteCartItem(cartItemId);
        }else{
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(cartItem.getProductVariant().getProduct().getPrice() * newQuantity);
        }
        CartItem c= this.cartItemRepository.save(cartItem);
        return convertToResponse(c);
    }


    public List<CartItemResponse> getAllCartItemByCartId(Long cartId) throws AppException {
        Cart cart = this.cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException("cart không tồn tại"));

        List<CartItem> cartItems = this.cartItemRepository.findByCart(cart);

        return cartItems.stream()
                .map(this::convertToResponse) // <-- Chuyển CartItem thành CartItemResponse
                .toList();
    }



}
