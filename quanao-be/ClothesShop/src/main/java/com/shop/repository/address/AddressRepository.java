package com.shop.repository.address;

import com.shop.entity.Address;
import com.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // Lấy tất cả địa chỉ của một User
    List<Address> findByUser(User user);
}