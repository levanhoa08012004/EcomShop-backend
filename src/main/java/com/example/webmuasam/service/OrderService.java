package com.example.webmuasam.service;

import com.example.webmuasam.dto.Request.CartItemDTO;
import com.example.webmuasam.dto.Request.InitPaymentRequest;
import com.example.webmuasam.dto.Request.OrderRequest;
import com.example.webmuasam.dto.Request.OrderRequestByCash;
import com.example.webmuasam.dto.Response.InitPaymentResponse;
import com.example.webmuasam.dto.Response.OrderResponse;
import com.example.webmuasam.dto.Response.OrderResponseByCash;
import com.example.webmuasam.dto.Response.ResultPaginationDTO;
import com.example.webmuasam.entity.*;
import com.example.webmuasam.exception.AppException;
import com.example.webmuasam.repository.*;
import com.example.webmuasam.util.SecurityUtil;
import com.example.webmuasam.util.constant.PaymentMethod;
import com.example.webmuasam.util.constant.StatusOrder;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Builder
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final PaymentService paymentService;
    public OrderService (OrderRepository orderRepository,ProductVariantRepository productVariantRepository,OrderDetailRepository orderDetailRepository,CartRepository cartRepository,CartItemRepository cartItemRepository,UserRepository userRepository,CartService cartService,PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }
    @Transactional
    public Order createOrderByCash(OrderRequestByCash orderRequest) throws AppException {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new AppException("Token không hợp lệ"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Email không tồn tại"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));

        // Copy list cartItem tránh lỗi khi xóa sau đó dùng lại
        List<CartItemDTO> items = cart.getCartItems().stream().map(ci -> new CartItemDTO(ci.getProductVariant().getId(),ci.getQuantity())).collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new AppException("Giỏ hàng đang trống");
        }

        // Lấy danh sách variant để kiểm kho
        List<Long> variantIds = items.stream()
                .map(item -> item.getVariantId())
                .distinct()
                .collect(Collectors.toList());

        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(variantIds);

        // Kiểm tra tồn kho từng sản phẩm
        for (CartItemDTO item : items) {
            ProductVariant variant = findVariant(variants, item.getVariantId());
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException("Sản phẩm " + variant.getProduct().getName() + " không đủ hàng");
            }
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setFullName(orderRequest.getFullName());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setEmail(orderRequest.getEmail());
        order.setAddress(orderRequest.getAddress());
        order.setStatus(StatusOrder.SUCCESS_ORDER);
        order.setPaymentMethod(PaymentMethod.COD);

        order = orderRepository.save(order); // lưu để lấy ID

        // Tạo OrderDetail và trừ tồn kho
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO item : items) {
            ProductVariant variant = findVariant(variants, item.getVariantId());

            // Trừ tồn kho
            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductVariant(variant);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(variant.getProduct().getPrice());

            orderDetails.add(detail);
        }

        orderDetailRepository.saveAll(orderDetails);

        // Xóa giỏ hàng
        cartService.clearCartByUserId(user.getId());

        log.info("Tạo đơn hàng thành công: orderId={}, user={}", order.getId(), user.getEmail());
        return order;
    }




    @Transactional
    public OrderResponse checkoutWithVnPay(OrderRequest orderRequest)throws AppException {
        // 1. Lấy user hiện tại từ token (qua email)
//        String email = SecurityUtil.getCurrentUserLogin()
//                .orElseThrow(() -> new AppException("Không thể xác thực người dùng"));
        User user = userRepository.findByEmail("hoa@gmail.com")
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng"));

        // 2. Lấy giỏ hàng
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException("Không tìm thấy giỏ hàng"));
        List<CartItem> cartItems = new ArrayList<>(cart.getCartItems()); // <-- Quan trọng!
        if (cartItems.isEmpty()) {
            throw new AppException("Giỏ hàng trống");
        }

        // 3. Lock các biến thể sản phẩm
        List<Long> variantIds = cartItems.stream()
                .map(item -> item.getProductVariant().getId())
                .toList();
        List<ProductVariant> variants = productVariantRepository.findAllByIdInForUpdate(variantIds);

        // 4. Kiểm tra tồn kho
        for (CartItem item : cartItems) {
            ProductVariant variant = findVariant(variants, item.getProductVariant().getId());
            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new AppException("Sản phẩm " + variant.getProduct().getName() + " không đủ số lượng");
            }
        }

        // 5. Tính tổng tiền
        double total = cartItems.stream()
                .mapToDouble(item -> item.getProductVariant().getProduct().getPrice() * item.getQuantity())
                .sum();

        // 6. Tạo đơn hàng (chưa trừ tồn kho vội)
        Order order = new Order();
        order.setUser(user);
        order.setStatus(StatusOrder.PENDING); // chờ thanh toán
        order.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setEmail(orderRequest.getEmail());
        order.setAddress(orderRequest.getAddress());
        order.setFullName(orderRequest.getFullName());
        order.setTotal_price(total);
        orderRepository.save(order);

        // 7. Tạo danh sách OrderDetail
        List<OrderDetail> orderDetails = cartItems.stream()
                .map(item -> {
                    ProductVariant variant = null;
                    try {
                        variant = findVariant(variants, item.getProductVariant().getId());
                    } catch (AppException e) {
                        throw new RuntimeException(e);
                    }

                    OrderDetail detail = new OrderDetail();
                    detail.setOrder(order);
                    detail.setProductVariant(variant);
                    detail.setQuantity(item.getQuantity());
                    return detail;
                }).toList();
        orderDetailRepository.saveAll(orderDetails);

        // 8. Tạo URL thanh toán VNPay
        InitPaymentRequest initPaymentRequest = InitPaymentRequest.builder()
                .userId(user.getId())
                .amount((long) total) // nhân 100 trong service VNPay rồi
                .txnRef(String.valueOf(order.getId())) // Gắn orderId để truy vết khi callback
                .requestId(orderRequest.getRequestId())
                .ipAddress(orderRequest.getIpAddress())
                .build();

        InitPaymentResponse paymentResponse = paymentService.init(initPaymentRequest);


        // 9. Trả về OrderResponse (FE dùng redirect đến VNPay)
        return new OrderResponse(
                order.getId(),
                order.getTotal_price(),
                order.getStatus(),
                order.getFullName(),
                order.getPhoneNumber(),
                order.getEmail(),
                order.getAddress(),
                paymentResponse
        );
    }

    private ProductVariant findVariant(List<ProductVariant> list, Long id) throws AppException{
        return list.stream()
                .filter(pv -> pv.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AppException("Không tìm thấy biến thể sản phẩm"));
    }



    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != StatusOrder.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ thanh toán");
        }

        // Xóa đơn hàng và chi tiết
        orderDetailRepository.deleteByOrderId(orderId);
        orderRepository.delete(order);
    }


    @Transactional
    public void confirmVnPayPayment(long txnRef, boolean success)throws AppException {
        Order order = orderRepository.findById(txnRef)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng"));

        if (!order.getStatus().equals(StatusOrder.PENDING)) return;

        if (success) {
            // Trừ hàng
            List<OrderDetail> details = orderDetailRepository.findByOrderId(txnRef);
            for (OrderDetail detail : details) {
                ProductVariant variant = detail.getProductVariant();
                variant.setStockQuantity(variant.getStockQuantity() - detail.getQuantity());
                productVariantRepository.save(variant);
            }

            Long userId= order.getUser().getId();
            this.cartService.clearCartByUserId(userId);
            order.setStatus(StatusOrder.SUCCESS_PAID);
        } else {
            order.setStatus(StatusOrder.FAILED);
        }
        orderRepository.save(order);
    }

    public ResultPaginationDTO getAllOrderByUser(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber());
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(orders.getTotalElements());
        meta.setPages(orders.getTotalPages());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(orders.getContent());

        return resultPaginationDTO;
    }


    public Order getOrderStatus(Long orderId) throws AppException {
        Order order;
        order = this.orderRepository.findById(orderId).orElseThrow(()->new AppException("order khong ton tai"));
        return order;
    }



}
