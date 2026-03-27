package com.shop.service.cart;

import com.shop.dto.cart.AddToCartRequestDTO;
import com.shop.dto.cart.CartItemDTO;
import com.shop.dto.cart.CartResponseDTO;
import com.shop.entity.*;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.cart.CartItemMapper;
import com.shop.repository.cart.CartItemRepository;
import com.shop.repository.cart.CartRepository;
import com.shop.repository.product.ProductRepository;
import com.shop.repository.product.ProductVariantRepository;
import com.shop.repository.user.UserRepository;
import com.shop.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final ProductVariantRepository productVariantRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponseDTO getCartForCurrentUser() {
        User currentUser = getCurrentUser();
        Cart cart = findCartByUser(currentUser);
        return mapCartToResponseDTO(cart);
    }


    @Override
    public CartResponseDTO addProductToCart(AddToCartRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        Cart cart = findCartByUser(currentUser);

        // 1. Tìm Variant thay vì Product
        // Sử dụng findById thông thường (sẽ áp dụng @Where(clause = "is_deleted = false"))
        ProductVariant variant = productVariantRepository.findById(requestDTO.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân loại sản phẩm: " + requestDTO.getVariantId()));

        // 2. Kiểm tra tồn kho của Variant
        if (variant.getStockQuantity() < requestDTO.getQuantity()) {
            throw new RuntimeException("Phân loại này không đủ hàng tồn kho");
        }

        // 3. Kiểm tra xem Variant này đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndVariant(cart, variant);

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + requestDTO.getQuantity();

            if (variant.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Không đủ hàng tồn kho");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant) // Set Variant
                    .quantity(requestDTO.getQuantity())
                    .build();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        return mapCartToResponseDTO(cart);
    }

    @Transactional
    public CartResponseDTO removeProductFromCart(Long cartItemId) {
        User currentUser = getCurrentUser();
        Cart cart = findCartByUser(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy item trong giỏ"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa vật phẩm này");
        }

        // Xóa đúng cách – dùng orphanRemoval
        cart.getItems().remove(cartItem);

        // Không cần gọi delete repository — Hibernate tự xóa
        // cartItemRepository.delete(cartItem);

        Cart updatedCart = findCartByUser(currentUser);
        return mapCartToResponseDTO(updatedCart);
    }


    @Override
    public CartResponseDTO updateProductQuantity(Long cartItemId, int quantity) {
        User currentUser = getCurrentUser();
        Cart cart = findCartByUser(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy item trong giỏ"));

        // Xác thực: Item này phải thuộc về giỏ hàng của user đang đăng nhập
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new SecurityException("Bạn không có quyền sửa vật phẩm này");
        }

        if (quantity <= 0) {
            // Nếu số lượng <= 0, xóa luôn item đó
            cartItemRepository.delete(cartItem);
        } else {
            // Lấy Variant từ CartItem để kiểm tra tồn kho cụ thể (Màu/Size)
            ProductVariant variant = cartItem.getVariant();

            if (variant.getStockQuantity() < quantity) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName()
                        + " (" + variant.getColor() + " - " + variant.getSize() + ") "
                        + "chỉ còn lại " + variant.getStockQuantity() + " sản phẩm.");
            }

            // Cập nhật số lượng mới
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        // Tải lại giỏ hàng để trả về dữ liệu mới nhất
        // (Cần thiết vì tổng tiền đã thay đổi)
        Cart updatedCart = findCartByUser(currentUser);
        return mapCartToResponseDTO(updatedCart);
    }

    // === CÁC HÀM HỖ TRỢ ===

    /**
     * Lấy User entity của người dùng đang đăng nhập
     */
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));
    }

    /**
     * Tìm giỏ hàng của User (đã bao gồm xử lý lỗi)
     */
    private Cart findCartByUser(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giỏ hàng cho user: " + user.getId()));
    }

    /**
     * Chuyển Cart Entity sang CartResponseDTO (bao gồm tính tổng tiền)
     */
    private CartResponseDTO mapCartToResponseDTO(Cart cart) {
        CartResponseDTO dto = new CartResponseDTO();
        dto.setId(cart.getId());

        List<CartItemDTO> itemDTOs = cartItemMapper.toDTOList(cart.getItems());
        dto.setItems(itemDTOs);

        // Tính tổng tiền
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItemDTO item : itemDTOs) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
        dto.setTotalPrice(totalPrice);

        return dto;
    }
}
