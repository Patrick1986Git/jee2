package com.company.shop.module.category.entity;

import org.hibernate.annotations.SQLRestriction;

import com.company.shop.common.model.SoftDeleteEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
@SQLRestriction("deleted = false") // Automatyczne filtrowanie usuniętych
public class Category extends SoftDeleteEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(nullable = false, unique = true, length = 150)
	private String slug; // Przyjazny URL, np. "telefony-i-akcesoria"

	@Column(length = 500)
	private String description;

	// Opcjonalne: Jeśli chcesz mieć drzewo kategorii
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Category parent;

	protected Category() {
		// JPA
	}

	public Category(String name, String slug, String description) {
		this.name = name;
		this.slug = slug;
		this.description = description;
	}

	// Konstruktor dla podkategorii
	public Category(String name, String slug, String description, Category parent) {
		this(name, slug, description);
		this.parent = parent;
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

	public Category getParent() {
		return parent;
	}

	// Metoda biznesowa do aktualizacji
	public void update(String name, String slug, String description, Category parent) {
		this.name = name;
		this.slug = slug;
		this.description = description;
		this.parent = parent;
	}
}