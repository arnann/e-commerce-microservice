package cn.edu.ecommerce.product.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.product.model.Category;
import cn.edu.ecommerce.product.model.ProductStatus;
import cn.edu.ecommerce.product.model.ProductSummary;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryProductRepository {
    private final IdGenerator categoryIds = new IdGenerator(1);
    private final IdGenerator productIds = new IdGenerator(100);
    private final Map<Long, Category> categories = new ConcurrentHashMap<>();
    private final Map<Long, ProductSummary> products = new ConcurrentHashMap<>();

    public Category createCategory(String name) {
        long id = categoryIds.nextId();
        Category category = new Category(id, name, Instant.now());
        categories.put(id, category);
        return category;
    }

    public ProductSummary createProduct(Long categoryId, String name, String description, BigDecimal price, int stock) {
        if (!categories.containsKey(categoryId)) {
            throw new IllegalArgumentException("category not found");
        }
        long id = productIds.nextId();
        ProductSummary product = new ProductSummary(
                id,
                categoryId,
                name,
                description,
                price,
                stock,
                ProductStatus.DRAFT,
                Instant.now()
        );
        products.put(id, product);
        return product;
    }

    public Optional<ProductSummary> findProduct(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public ProductSummary save(ProductSummary product) {
        products.put(product.id(), product);
        return product;
    }

    public List<ProductSummary> findAll() {
        return products.values().stream()
                .sorted(Comparator.comparing(ProductSummary::id))
                .toList();
    }
}
