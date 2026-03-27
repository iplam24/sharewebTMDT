package com.shop.service.discount;

import com.shop.dto.discount.DiscountDTO;
import com.shop.entity.Discount;
import com.shop.entity.User;
import com.shop.enums.DiscountType;
import com.shop.exception.ResourceNotFoundException;
import com.shop.repository.discount.DiscountRepository;
import com.shop.repository.order.OrderRepository;
import com.shop.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Discount createDiscount(DiscountDTO discountDTO) {
        if (discountRepository.existsByCode(discountDTO.getCode())) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }

        Discount discount = Discount.builder()
                .code(discountDTO.getCode())
                .type(discountDTO.getType())
                .value(discountDTO.getValue())
                .minOrderAmount(discountDTO.getMinOrderAmount())
                .maxDiscountAmount(discountDTO.getMaxDiscountAmount())
                .startDate(discountDTO.getStartDate())
                .endDate(discountDTO.getEndDate())
                .usageLimit(discountDTO.getUsageLimit())
                .usedCount(0)
                .isActive(discountDTO.isActive())
                .build();

        if (discountDTO.getAllowedUserIds() != null && !discountDTO.getAllowedUserIds().isEmpty()) {
            List<User> users = userRepository.findAllById(discountDTO.getAllowedUserIds());
            discount.setAllowedUsers(users);
        }

        return discountRepository.save(discount);
    }

    @Override
    @Transactional
    public Discount updateDiscount(Long id, DiscountDTO discountDTO) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã giảm giá với id: " + id));

        discount.setCode(discountDTO.getCode());
        discount.setType(discountDTO.getType());
        discount.setValue(discountDTO.getValue());
        discount.setMinOrderAmount(discountDTO.getMinOrderAmount());
        discount.setMaxDiscountAmount(discountDTO.getMaxDiscountAmount());
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());
        discount.setUsageLimit(discountDTO.getUsageLimit());
        discount.setActive(discountDTO.isActive());

        if (discountDTO.getAllowedUserIds() != null) {
            List<User> users = userRepository.findAllById(discountDTO.getAllowedUserIds());
            discount.setAllowedUsers(users);
        } else {
            discount.getAllowedUsers().clear();
        }

        return discountRepository.save(discount);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        if (!discountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy mã giảm giá với id: " + id);
        }
        discountRepository.deleteById(id);
    }

    @Override
    public Discount getDiscountByCode(String code) {
        return discountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại: " + code));
    }

    @Override
    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    @Override
    public BigDecimal calculateDiscountAmount(Discount discount, BigDecimal orderTotal) {
        if (discount.getMinOrderAmount() != null && orderTotal.compareTo(discount.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount.getType() == DiscountType.PERCENTAGE) {
            discountAmount = orderTotal.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            if (discount.getMaxDiscountAmount() != null && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
                discountAmount = discount.getMaxDiscountAmount();
            }
        } else if (discount.getType() == DiscountType.FIXED_AMOUNT) {
            discountAmount = discount.getValue();
        }

        return discountAmount;
    }

    @Override
    public void validateDiscount(Discount discount, User user) {
        // 1. Kiểm tra trạng thái kích hoạt
        if (!discount.isActive()) {
            throw new RuntimeException("Mã giảm giá này hiện đang bị khóa.");
        }

        // 2. Kiểm tra thời gian
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate())) {
            throw new RuntimeException("Mã giảm giá chưa đến thời gian hiệu lực.");
        }
        if (now.isAfter(discount.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn.");
        }

        // 3. Kiểm tra số lượt sử dụng TỔNG CỘNG
        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng.");
        }

        // 4. Kiểm tra người dùng được phép (nếu có danh sách cụ thể)
        if (discount.getAllowedUsers() != null && !discount.getAllowedUsers().isEmpty()) {
            boolean isAllowed = discount.getAllowedUsers().stream()
                    .anyMatch(u -> u.getId().equals(user.getId()));
            if (!isAllowed) {
                throw new RuntimeException("Mã giảm giá này không áp dụng cho tài khoản của bạn.");
            }
        }

        // 5. Kiểm tra xem user này ĐÃ DÙNG mã này chưa (MỚI THÊM)
        // Giả sử mỗi user chỉ được dùng 1 lần cho mỗi mã giảm giá
        long usageCount = orderRepository.countByUserAndDiscountAndStatusNotCancelled(user, discount);
        if (usageCount > 0) {
            throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi.");
        }
    }

    @Override
    @Transactional
    public void incrementUsageCount(Discount discount) {
        discount.setUsedCount(discount.getUsedCount() + 1);
        discountRepository.save(discount);
    }
}
