package com.thred.datingapp.inApp.repository;

import com.thred.datingapp.common.entity.inApp.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByInAppProductId(String inAppProductId);
    Optional<Product> findById(Long productId);
}
