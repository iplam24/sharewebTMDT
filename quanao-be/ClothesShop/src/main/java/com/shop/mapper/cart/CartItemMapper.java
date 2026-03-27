package com.shop.mapper.cart;

import com.shop.dto.cart.CartItemDTO;
import com.shop.entity.CartItem;
import com.shop.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    // Lấy thông tin từ Variant và Product cha của nó
    @Mapping(target = "productId", source = "variant.product.id")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "price", source = "variant.product.price")
    @Mapping(target = "imageUrl", source = "variant.product.images", qualifiedByName = "firstImageToUrl")
    // Map thêm Color và Size
    @Mapping(target = "color", source = "variant.color")
    @Mapping(target = "size", source = "variant.size")
    CartItemDTO toDTO(CartItem cartItem);

    List<CartItemDTO> toDTOList(List<CartItem> cartItems);

    @Named("firstImageToUrl")
    default String firstImageToUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getImageUrl();
    }
}