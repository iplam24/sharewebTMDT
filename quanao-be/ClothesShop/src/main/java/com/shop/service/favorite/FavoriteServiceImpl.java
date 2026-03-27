package com.shop.service.favorite;

import com.shop.entity.Favorite;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.exception.ResourceNotFoundException;
import com.shop.repository.favorite.FavoriteRepository;
import com.shop.repository.product.ProductRepository;
import com.shop.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // mặc định các hàm chỉ đọc
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Override
    public List<Favorite> getMyFavorites() {
        User user = getCurrentUser();
        return favoriteRepository.findByUser(user);
    }

    @Override
    @Transactional // ghi đè readOnly = false
    public void addToFavorite(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            return;
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();

        favoriteRepository.save(favorite);
    }

    @Override
    @Transactional // QUAN TRỌNG để xoá có transaction
    public void removeFromFavorite(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        favoriteRepository.deleteByUserAndProduct(user, product);
    }
}
