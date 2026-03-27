package com.shop.repository.product;

import com.shop.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // Ghi đè hàm findById để thêm khóa
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    Optional<ProductVariant> findById(Long id);

    // Thêm phương thức tìm kiếm bỏ qua @Where(clause = "is_deleted = false")
    @Query(value = "SELECT * FROM product_variants WHERE id = :id", nativeQuery = true)
    Optional<ProductVariant> findByIdIncludingDeleted(@Param("id") Long id);

    // Tính tổng tồn kho của tất cả sản phẩm
    @Query("SELECT SUM(pv.stockQuantity) FROM ProductVariant pv")
    Long sumTotalStock();
}
