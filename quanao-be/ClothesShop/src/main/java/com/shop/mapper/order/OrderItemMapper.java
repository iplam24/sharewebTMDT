package com.shop.mapper.order;

import com.shop.dto.order.OrderItemResponseDTO;
import com.shop.entity.OrderItem;
import com.shop.service.product.ProductImageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class OrderItemMapper {

    @Autowired
    protected ProductImageService productImageService;

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "imageUrl",
            expression = "java(productImageService.getFirstImageUrl(orderItem.getProductId()))")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "size", source = "size")
    public abstract OrderItemResponseDTO toDTO(OrderItem orderItem);

    public abstract List<OrderItemResponseDTO> toDTOList(List<OrderItem> orderItems);
}
