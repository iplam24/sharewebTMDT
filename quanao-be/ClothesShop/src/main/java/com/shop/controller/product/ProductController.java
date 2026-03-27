package com.shop.controller.product;

import com.shop.dto.product.ProductDTO;
import com.shop.dto.product.ProductRequestDTO;
import com.shop.dto.response.PageResponseDTO;
import com.shop.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    // lấy ra trang sản phẩm mặc định là trang 0 và size của trang là 10 sản phẩm
    @GetMapping
    public ResponseEntity<PageResponseDTO<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category
    ) {
        PageResponseDTO<ProductDTO> products = productService.getProductsPage(page, size, category);
        return ResponseEntity.ok(products);
    }

// controller lấy về sản phẩm theo id( productdetail)
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductDTO newProduct = productService.createProduct(requestDTO);
        // Trả về 201 Created cùng với thông tin sản phẩm vừa tạo
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }


    //Sửa sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO requestDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, requestDTO);
        return ResponseEntity.ok(updatedProduct);
    }


    //delete xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        // Trả về 204 No Content
        return ResponseEntity.noContent().build();
    }
}