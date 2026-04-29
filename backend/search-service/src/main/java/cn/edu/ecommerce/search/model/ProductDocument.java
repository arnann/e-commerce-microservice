package cn.edu.ecommerce.search.model;

import java.math.BigDecimal;

public record ProductDocument(
        Long id,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int sales,
        int views
) {
    public boolean matches(String keyword, Long targetCategoryId) {
        boolean categoryMatches = targetCategoryId == null || categoryId.equals(targetCategoryId);
        if (!categoryMatches) {
            return false;
        }
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        return name.toLowerCase().contains(normalized) || description.toLowerCase().contains(normalized);
    }
}
