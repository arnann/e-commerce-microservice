package cn.edu.ecommerce.search.service;

import cn.edu.ecommerce.search.model.ProductDocument;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SearchRecommendationService {
    private final InMemorySearchIndex searchIndex;

    public SearchRecommendationService(InMemorySearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }

    public void index(ProductDocument document) {
        if (document.id() == null || document.categoryId() == null) {
            throw new IllegalArgumentException("product id and category id required");
        }
        searchIndex.save(document);
    }

    public List<ProductDocument> search(String keyword, Long categoryId, String sort) {
        Comparator<ProductDocument> comparator = switch (sort == null ? "" : sort) {
            case "priceAsc" -> Comparator.comparing(ProductDocument::price);
            case "priceDesc" -> Comparator.comparing(ProductDocument::price).reversed();
            case "viewsDesc" -> Comparator.comparing(ProductDocument::views).reversed();
            default -> Comparator.comparing(ProductDocument::sales).reversed();
        };
        return searchIndex.findAll().stream()
                .filter(document -> document.matches(keyword, categoryId))
                .sorted(comparator.thenComparing(ProductDocument::id))
                .toList();
    }

    public List<ProductDocument> recommendForCategory(Long categoryId, Long excludeProductId) {
        return searchIndex.findAll().stream()
                .filter(document -> document.categoryId().equals(categoryId))
                .filter(document -> !document.id().equals(excludeProductId))
                .sorted(Comparator.comparing(ProductDocument::views).reversed())
                .limit(6)
                .toList();
    }
}
