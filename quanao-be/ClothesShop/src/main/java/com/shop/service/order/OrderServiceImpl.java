package com.shop.service.order;

import com.shop.dto.order.CheckoutRequestDTO;
import com.shop.dto.order.OrderResponseDTO;
import com.shop.dto.response.PageResponseDTO;
import com.shop.entity.*;
import com.shop.enums.OrderStatus;
import com.shop.enums.PaymentMethod;
import com.shop.enums.PaymentStatus;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.order.OrderMapper;

import com.shop.repository.discount.DiscountRepository;
import com.shop.repository.address.AddressRepository;
import com.shop.repository.cart.CartRepository;
import com.shop.repository.order.OrderRepository;
import com.shop.repository.product.ProductRepository;
import com.shop.repository.product.ProductVariantRepository;
import com.shop.repository.user.UserRepository;
import com.shop.service.EmailService;
import com.shop.service.discount.DiscountService;
import com.shop.service.order.OrderService;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- RẤT QUAN TRỌNG

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ProductVariantRepository productVariantRepository;
    private final DiscountService discountService;
    private final EmailService emailService; // <-- Thêm EmailService

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> getOrderHistoryForCurrentUser(
            int page, int size, String status
    ) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

        Page<Order> orderPage;

        // Logic lọc theo trạng thái
        if (status != null && !status.isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderRepository.findByUserAndStatusOrderByOrderDateDesc(
                        currentUser, orderStatus, pageable
                );
            } catch (IllegalArgumentException e) {
                orderPage = orderRepository.findByUserOrderByOrderDateDesc(currentUser, pageable);
            }
        } else {
            orderPage = orderRepository.findByUserOrderByOrderDateDesc(currentUser, pageable);
        }

        List<OrderResponseDTO> dtoList = orderMapper.toDTOList(orderPage.getContent());

        return new PageResponseDTO<>(
                dtoList,
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                orderPage.getNumber() + 1,
                orderPage.getSize()
        );
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(CheckoutRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng."));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang rỗng.");
        }

        Address shippingAddress = addressRepository.findById(requestDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ."));

        if (!shippingAddress.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Địa chỉ giao hàng không hợp lệ.");
        }
        String formattedAddress = formatAddress(shippingAddress);

        Order newOrder = Order.builder()
                .user(currentUser)
                .shippingAddress(formattedAddress)
                .phoneNumber(shippingAddress.getPhoneNumber())
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .paymentMethod(PaymentMethod.valueOf(requestDTO.getPaymentMethod()))
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        BigDecimal runningTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Long variantId = cartItem.getVariant().getId();
            ProductVariant lockedVariant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại hoặc đã bị xóa."));

            Product product = lockedVariant.getProduct();

            if (lockedVariant.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() +
                        " (" + lockedVariant.getColor() + " - " + lockedVariant.getSize() + ") không đủ tồn kho.");
            }

            lockedVariant.setStockQuantity(lockedVariant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(lockedVariant);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .color(lockedVariant.getColor())
                    .size(lockedVariant.getSize())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            newOrder.addItem(orderItem);

            runningTotal = runningTotal.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        if (requestDTO.getDiscountCode() != null && !requestDTO.getDiscountCode().isEmpty()) {
            Discount discount = discountService.getDiscountByCode(requestDTO.getDiscountCode());
            discountService.validateDiscount(discount, currentUser);
            BigDecimal discountAmount = discountService.calculateDiscountAmount(discount, runningTotal);
            
            if (discount.getMinOrderAmount() != null && runningTotal.compareTo(discount.getMinOrderAmount()) < 0) {
                 throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này (" + discount.getMinOrderAmount() + ")");
            }

            runningTotal = runningTotal.subtract(discountAmount);
            if (runningTotal.compareTo(BigDecimal.ZERO) < 0) {
                runningTotal = BigDecimal.ZERO;
            }
            newOrder.setDiscount(discount);
            newOrder.setDiscountAmount(discountAmount);
            discountService.incrementUsageCount(discount);
        }

        newOrder.setTotalPrice(runningTotal);

        cart.getItems().clear();
        cartRepository.save(cart);

        Order savedOrder = orderRepository.save(newOrder);

        // Gửi email thông báo cho Admin
        List<String> adminEmails = userRepository.findAllAdminEmails();
        if (!adminEmails.isEmpty()) {
            emailService.sendNewOrderNotificationToAdmins(adminEmails, savedOrder);
        }

        return orderMapper.toDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return orderMapper.toDTOList(orders);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDTO(updatedOrder);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));
    }

    private String formatAddress(Address address) {
        return String.format("%s, %s, %s, %s",
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getCountry());
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền hủy đơn hàng này.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Đơn hàng đã được xử lý hoặc đang giao, không thể hủy.");
        }

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.getVariants().stream()
                        .filter(v -> v.getColor().equals(item.getColor()) && v.getSize().equals(item.getSize()))
                        .findFirst()
                        .ifPresent(variant -> {
                            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                        });
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void confirmPayment(Long orderId, PaymentStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
        order.setDeliveryDate(LocalDateTime.now());
        order.setPaymentStatus(status);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO confirmDelivery(Long orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng."));

        // 1. Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Bạn không có quyền xác nhận đơn hàng này.");
        }

        // 2. Kiểm tra trạng thái: Chỉ được xác nhận khi đang SHIPPED
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Chỉ có thể xác nhận khi đơn hàng đang trong trạng thái Đang giao.");
        }

        // 3. Cập nhật trạng thái
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveryDate(LocalDateTime.now());

        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với id: " + orderId));
        return orderMapper.toDTO(order);
    }
}
