package com.shop.controller.address;

import com.shop.dto.address.AddressDTO;
import com.shop.service.address.AddressService; // Import Service interface
import jakarta.validation.Valid; // Nên thêm validation
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    // Inject Service thay vì Repository
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressDTO>> myAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @PostMapping
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddressDTO req) {
        addressService.addNewAddress(req);
        return ResponseEntity.ok("Thêm địa chỉ thành công");
    }
}