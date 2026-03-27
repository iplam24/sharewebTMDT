package com.shop.service.discount;

import com.shop.dto.discount.DiscountDTO;
import com.shop.entity.Discount;
import com.shop.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {
    Discount createDiscount(DiscountDTO discountDTO);
    Discount updateDiscount(Long id, DiscountDTO discountDTO);
    void deleteDiscount(Long id);
    Discount getDiscountByCode(String code);
    List<Discount> getAllDiscounts();
    BigDecimal calculateDiscountAmount(Discount discount, BigDecimal orderTotal);
    
    // Đổi từ boolean sang void để ném Exception cụ thể
    void validateDiscount(Discount discount, User user);

    void incrementUsageCount(Discount discount);
}
