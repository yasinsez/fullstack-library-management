package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.CategoryDTO;
import com.yasinsez.library.model.Category;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDTO(
                category.getId(),
                category.getName());
    }

    public static Category toEntity(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryDTO.id);
        category.setName(categoryDTO.name);
        return category;
    }
}
