package com.shop.service.product;

import com.shop.dto.product.ProductDTO;
import com.shop.dto.product.ProductRequestDTO;
import com.shop.dto.product.ProductVariantDTO;
import com.shop.dto.response.PageResponseDTO;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.entity.ProductVariant;
import com.shop.exception.ResourceNotFoundException;
import com.shop.mapper.product.ProductMapper;
import com.shop.repository.category.CategoryRepository;
import com.shop.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

//    @Override
//    @Transactional(readOnly = true)
//    public List<ProductDTO> getAllProducts() {
//        return productMapper.toDTOList(productRepository.findAll());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public ProductDTO getProductById(Long id) {
//        Product product = productRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
//        return productMapper.toDTO(product);
//    }

@Override
@Transactional(readOnly = true)
public PageResponseDTO<ProductDTO> getProductsPage(int page, int size, String category) {
    // Sắp xếp theo ID mới nhất
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

    Page<Product> productPage;

    // Logic Lọc
    if (category != null && !category.isEmpty()) {
        productPage = productRepository.findByCategory_NameIgnoreCase(category, pageable);
    } else {
        productPage = productRepository.findAll(pageable);
    }

    // Map Entity Page sang DTO PageResponseDTO
    List<ProductDTO> dtoList = productMapper.toDTOList(productPage.getContent());

    return new PageResponseDTO<>(
            dtoList,
            productPage.getTotalPages(),
            productPage.getTotalElements(),
            productPage.getNumber() + 1, // Trả về 1-indexed
            productPage.getSize()
    );
}

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return productMapper.toDTO(product);
    }

    @Override
    public PageResponseDTO<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String category) {
        return List.of();
    }

    @Override
    public ProductDTO createProduct(ProductRequestDTO requestDTO) {
        // 1. TÌM CATEGORY BẰNG ID
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + requestDTO.getCategoryId()));
        Product product = productMapper.toEntity(requestDTO);
// 3. GẮN CATEGORY VÀO ENTITY
        product.setCategory(category);
        // 1. Xử lý ảnh
        if (requestDTO.getImageUrls() != null) {
            for (String url : requestDTO.getImageUrls()) {
                product.addImage(url);
            }
        }

        // 2. Xử lý Variants (Màu/Size)
        if (requestDTO.getVariants() != null) {
            for (ProductVariantDTO variantDTO : requestDTO.getVariants()) {
                product.addVariant(
                        variantDTO.getColor(),
                        variantDTO.getSize(),
                        variantDTO.getStockQuantity()
                );
            }
        }

        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // Update thông tin cơ bản
        productMapper.updateEntityFromDTO(requestDTO, existingProduct);

        // Update ảnh (Giữ nguyên logic cũ)
        existingProduct.getImages().clear();
        if (requestDTO.getImageUrls() != null) {
            for (String url : requestDTO.getImageUrls()) {
                existingProduct.addImage(url);
            }
        }

        // === LOGIC MỚI: SMART UPDATE VARIANTS ===
        List<ProductVariantDTO> newVariants = requestDTO.getVariants();
        List<ProductVariant> currentVariants = existingProduct.getVariants();

        if (newVariants != null) {
            // 1. Xóa những variant không còn trong list mới
            // (Nếu đã cấu hình Bước 1, nó sẽ là Soft Delete -> Không lỗi)
            currentVariants.removeIf(existingVar ->
                    newVariants.stream().noneMatch(newVar ->
                            // Nếu existingVar có ID, kiểm tra xem ID đó có trong list mới không
                            existingVar.getId() != null && existingVar.getId().equals(newVar.getId())
                    )
            );

            // 2. Cập nhật hoặc Thêm mới
            for (ProductVariantDTO newVarDTO : newVariants) {
                if (newVarDTO.getId() != null) {
                    // Tìm variant cũ để cập nhật
                    ProductVariant existingVar = currentVariants.stream()
                            .filter(v -> v.getId().equals(newVarDTO.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existingVar != null) {
                        // Cập nhật thông tin
                        existingVar.setColor(newVarDTO.getColor());
                        existingVar.setSize(newVarDTO.getSize());
                        existingVar.setStockQuantity(newVarDTO.getStockQuantity());
                    }
                } else {
                    // Nếu không có ID -> Thêm mới
                    existingProduct.addVariant(
                            newVarDTO.getColor(),
                            newVarDTO.getSize(),
                            newVarDTO.getStockQuantity()
                    );
                }
            }
        }
        // ========================================

        return productMapper.toDTO(productRepository.save(existingProduct));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepository.delete(product);
    }
}