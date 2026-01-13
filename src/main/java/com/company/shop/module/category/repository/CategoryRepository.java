package com.company.shop.module.category.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.company.shop.module.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

	/**
	 * Pozwala znaleźć kategorię po jej przyjaznym adresie URL (slug). Kluczowe dla
	 * frontendu sklepu.
	 */
	Optional<Category> findBySlug(String slug);

	/**
	 * Sprawdza, czy kategoria o danej nazwie już istnieje. Zapobiega duplikatom
	 * przed zapisem.
	 */
	boolean existsByName(String name);

	/**
	 * Sprawdza, czy dany slug jest już zajęty. Ważne podczas generowania unikalnych
	 * adresów URL.
	 */
	boolean existsBySlug(String slug);
}