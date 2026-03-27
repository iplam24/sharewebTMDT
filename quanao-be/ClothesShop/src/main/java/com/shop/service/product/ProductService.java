package com.shop.service.product;

import com.shop.dto.product.ProductDTO;
import com.shop.dto.product.ProductRequestDTO;
import com.shop.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    //List<ProductDTO> getAllProducts();
    // HÀM MỚI: Trả về PageResponseDTO cho Frontend
    PageResponseDTO<ProductDTO> getProductsPage(int page, int size, String category);
    ProductDTO getProductById(Long id);

    //Search sản phẩm
    PageResponseDTO<ProductDTO> searchProducts(String keyword, Pageable pageable);

    List<ProductDTO> getProductsByCategory(String category);

    ProductDTO createProduct(ProductRequestDTO requestDTO);

    ProductDTO updateProduct(Long id, ProductRequestDTO requestDTO);

    void deleteProduct(Long id);


}