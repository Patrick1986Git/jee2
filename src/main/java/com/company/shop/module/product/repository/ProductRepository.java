package com.company.shop.module.product.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.shop.module.product.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	Optional<Product> findBySlug(String slug);

	Optional<Product> findBySku(String sku);

	Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

	Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

	boolean existsBySku(String sku);

	boolean existsBySlug(String slug);
}