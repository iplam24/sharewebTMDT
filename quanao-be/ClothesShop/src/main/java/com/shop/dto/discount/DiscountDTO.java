package com.shop.dto.discount;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shop.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountDTO {
    private Long id;
    private String code;
    private DiscountType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;

    @JsonProperty("isActive") // <--- THÊM DÒNG NÀY
    private boolean isActive;
    
    private List<Long> allowedUserIds;
}
