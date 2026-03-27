package com.shop.service.favorite;

import com.shop.entity.Favorite;

import java.util.List;

public interface FavoriteService {
    List<Favorite> getMyFavorites();
    void addToFavorite(Long productId);
    void removeFromFavorite(Long productId);
}
