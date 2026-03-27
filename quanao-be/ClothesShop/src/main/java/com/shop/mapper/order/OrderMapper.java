package com.shop.mapper.order;

import com.shop.dto.order.OrderResponseDTO;
import com.shop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; // Import cái này

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    // === THÊM DÒNG MAPPING NÀY ===
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "discount.code", target = "discountCode")
    @Mapping(source = "phoneNumber", target = "phoneNumber") // <-- Map số điện thoại
    OrderResponseDTO toDTO(Order order);

    List<OrderResponseDTO> toDTOList(List<Order> orders);
}
