package com.shop.repository.user;

import com.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByCreatedAtAfter(LocalDateTime date);

    // Lấy danh sách tất cả email của user
    @Query("SELECT u.email FROM User u")
    List<String> findAllEmails();

    // Lấy danh sách email của tất cả ADMIN
    @Query("SELECT u.email FROM User u WHERE u.role = 'ADMIN'")
    List<String> findAllAdminEmails();
}
