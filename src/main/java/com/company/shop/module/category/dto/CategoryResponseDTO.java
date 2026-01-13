package com.company.shop.module.category.dto;

import java.util.UUID;

/**
 * DTO wyjściowe dla kategorii. Niemutowalne (immutable) dla zapewnienia
 * spójności danych wysyłanych do klienta.
 */
public class CategoryResponseDTO {

	private final UUID id;
	private final String name;
	private final String slug;
	private final String description;
	private final String parentName; // Czytelna informacja o kategorii nadrzędnej

	public CategoryResponseDTO(UUID id, String name, String slug, String description, String parentName) {
		this.id = id;
		this.name = name;
		this.slug = slug;
		this.description = description;
		this.parentName = parentName;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}

	public String getDescription() {
		return description;
	}

	public String getParentName() {
		return parentName;
	}
}