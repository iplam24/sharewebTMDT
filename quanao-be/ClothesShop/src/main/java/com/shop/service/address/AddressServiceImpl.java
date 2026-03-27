package com.shop.service.address;

import com.shop.dto.address.AddressDTO;
import com.shop.entity.Address;
import com.shop.entity.User;
import com.shop.mapper.address.AddressMapper;
import com.shop.repository.address.AddressRepository;
import com.shop.repository.user.UserRepository;
import com.shop.service.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    // Hàm hỗ trợ lấy User đang đăng nhập
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    @Override
    public List<AddressDTO> getMyAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUser(user).stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void addNewAddress(AddressDTO req) {
        User user = getCurrentUser();

        Address address = Address.builder()
                .user(user)
                .street(req.getStreet())
                .city(req.getCity())
                .state(req.getState())
                .country(req.getCountry() != null ? req.getCountry() : "Vietnam")
                .phoneNumber(req.getPhoneNumber())
                .build();

        addressRepository.save(address);
    }
}