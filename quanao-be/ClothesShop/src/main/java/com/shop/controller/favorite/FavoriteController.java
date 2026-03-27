package com.shop.controller.favorite;

import com.shop.dto.product.ProductDTO;
import com.shop.entity.Favorite;
import com.shop.mapper.product.ProductMapper;
import com.shop.service.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getMyFavorites() {
        List<Favorite> favorites = favoriteService.getMyFavorites();

        List<ProductDTO> products = favorites.stream()
                .map(f -> productMapper.toDTO(f.getProduct()))
                .toList();

        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Void> add(@PathVariable Long productId) {
        favoriteService.addToFavorite(productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        favoriteService.removeFromFavorite(productId);
        return ResponseEntity.noContent().build();
    }
}
