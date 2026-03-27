package com.shop.repository.product;

import com.shop.entity.Product;
import org.springframework.data.domain.Page; // <-- Import này
import org.springframework.data.domain.Pageable; // <-- Import này
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategory_NameIgnoreCase(String categoryName, Pageable pageable);

}