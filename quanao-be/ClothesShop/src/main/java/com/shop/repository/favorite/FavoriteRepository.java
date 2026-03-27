package com.shop.repository.favorite;

import com.shop.entity.Favorite;
import com.shop.entity.Product;
import com.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Chỉ lấy những Favorite mà Product chưa bị xóa mềm
    @Query("SELECT f FROM Favorite f JOIN FETCH f.product p WHERE f.user = :user AND p.isDeleted = false")
    List<Favorite> findByUser(@Param("user") User user);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
