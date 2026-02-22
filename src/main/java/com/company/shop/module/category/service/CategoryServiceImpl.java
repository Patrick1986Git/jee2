package com.company.shop.module.category.service;

import java.util.UUID;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.category.dto.CategoryCreateDTO;
import com.company.shop.module.category.dto.CategoryResponseDTO;
import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.mapper.CategoryMapper;
import com.company.shop.module.category.repository.CategoryRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    // Pattern do usuwania znaków specjalnych przy generowaniu sluga
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public CategoryServiceImpl(CategoryRepository repo, CategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponseDTO> findAll(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(UUID id) {
        return repo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findBySlug(String slug) {
        return repo.findBySlug(slug)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with slug: " + slug));
    }

    @Override
    public CategoryResponseDTO create(CategoryCreateDTO dto) {
        if (repo.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        String slug = generateSlug(dto.getName());
        
        // Obsługa kategorii nadrzędnej (jeśli podano parentId)
        Category parent = null;
        if (dto.getParentId() != null) {
            parent = repo.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        Category category = new Category(dto.getName(), slug, dto.getDescription(), parent);
        return mapper.toDto(repo.save(category));
    }

    @Override
    public CategoryResponseDTO update(UUID id, CategoryCreateDTO dto) {
        Category category = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with ID: " + id));

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = repo.findById(dto.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
        }

        String newSlug = generateSlug(dto.getName());
        category.update(dto.getName(), newSlug, dto.getDescription(), parent);
        
        return mapper.toDto(category);
    }

    @Override
    public void delete(UUID id) {
        Category category = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        category.delete(); // Używamy metody z Twojej encji
    }

    /**
     * Prywatna metoda pomocnicza do generowania "przyjaznych adresów URL"
     * Przykład: "Telefony i Akcesoria" -> "telefony-i-akcesoria"
     */
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}