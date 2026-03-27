package com.shop.service.address;

import com.shop.dto.address.AddressDTO;
import java.util.List;

public interface AddressService {
    // Lấy danh sách địa chỉ của user đang đăng nhập
    List<AddressDTO> getMyAddresses();

    // Thêm địa chỉ mới
    void addNewAddress(AddressDTO addressDTO);
}