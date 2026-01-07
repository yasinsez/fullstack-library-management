package com.yasinsez.library.service;

import com.yasinsez.library.model.Category;
import com.yasinsez.library.repository.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.listAll();
    }
}
