package com.shop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RevenueByDateDTO {
    private LocalDate date;
    private BigDecimal totalRevenue;
}