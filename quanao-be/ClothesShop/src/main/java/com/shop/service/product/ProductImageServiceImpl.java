package com.shop.service.product;

import com.shop.repository.product.ProductRepository;
import com.shop.service.product.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;

    @Override
    public String getFirstImageUrl(Long productId) {
        // FindById sẽ tải Product Entity (và quan hệ @OneToMany với ProductImage)
        return productRepository.findById(productId)
                .flatMap(product -> product.getImages().stream().findFirst()) // Lấy ảnh đầu tiên
                .map(image -> image.getImageUrl())
                .orElse(null);
    }
}