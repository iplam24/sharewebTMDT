package com.shop.mapper.address;

import com.shop.dto.address.AddressDTO;
import com.shop.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDTO toDTO(Address address);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    Address toEntity(AddressDTO dto);
}