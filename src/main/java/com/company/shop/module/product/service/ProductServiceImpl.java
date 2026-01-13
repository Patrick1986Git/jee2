package com.company.shop.module.product.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.shop.module.category.entity.Category;
import com.company.shop.module.category.repository.CategoryRepository;
import com.company.shop.module.product.dto.ProductCreateDTO;
import com.company.shop.module.product.dto.ProductResponseDTO;
import com.company.shop.module.product.entity.Product;
import com.company.shop.module.product.mapper.ProductMapper;
import com.company.shop.module.product.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ProductMapper mapper;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public ProductServiceImpl(ProductRepository productRepo, 
                              CategoryRepository categoryRepo, 
                              ProductMapper mapper) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        return productRepo.findAll(pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> findAllByCategory(UUID categoryId, Pageable pageable) {
        return productRepo.findByCategoryId(categoryId, pageable).map(mapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(UUID id) {
        return productRepo.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Produkt o podanym ID nie istnieje"));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findBySlug(String slug) {
        return productRepo.findBySlug(slug)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Produkt o slugu " + slug + " nie istnieje"));
    }

    @Override
    public ProductResponseDTO create(ProductCreateDTO dto) {
        // 1. Walidacja unikalności SKU (Klucz biznesowy)
        if (productRepo.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("Produkt z kodem SKU " + dto.getSku() + " już istnieje");
        }

        // 2. Pobranie kategorii
        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kategorii o ID: " + dto.getCategoryId()));

        // 3. Generowanie unikalnego sluga
        String slug = generateSlug(dto.getName());
        if (productRepo.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }

        // 4. Tworzenie encji (zgodnie z Twoim nowym konstruktorem)
        Product product = new Product(
                dto.getName(),
                slug,
                dto.getSku(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStock(),
                category
        );

        return mapper.toDto(productRepo.save(product));
    }

    @Override
    public ProductResponseDTO update(UUID id, ProductCreateDTO dto) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nie można zaktualizować. Produkt nie istnieje."));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kategorii"));

        // Aktualizacja danych biznesowych
        String newSlug = generateSlug(dto.getName());
        product.update(dto.getName(), newSlug, dto.getDescription(), dto.getPrice(), dto.getStock(), category);

        return mapper.toDto(product);
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produkt nie znaleziony"));
        product.delete(); // Soft Delete
    }

    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}