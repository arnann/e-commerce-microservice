package cn.edu.ecommerce.product.service;

import cn.edu.ecommerce.product.model.Category;
import cn.edu.ecommerce.product.model.ProductStatus;
import cn.edu.ecommerce.product.model.ProductSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductCatalogService {
    private final ProductRepository repository;

    public ProductCatalogService(ProductRepository repository) {
        this.repository = repository;
    }

    public Category createCategory(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("category name required");
        }
        return repository.createCategory(name.trim());
    }

    public ProductSummary createProduct(Long categoryId, String name, String description, BigDecimal price, int stock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("product name required");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("positive price required");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
        return repository.createProduct(categoryId, name.trim(), description, price, stock);
    }

    public List<Category> listCategories() {
        return repository.findCategories();
    }

    public ProductSummary publish(Long productId) {
        ProductSummary product = getProduct(productId);
        if (product.stock() <= 0) {
            throw new IllegalStateException("cannot publish product without stock");
        }
        return repository.save(product.withStatus(ProductStatus.ON_SALE));
    }

    public ProductSummary updateProduct(Long productId, Long categoryId, String name, String description, BigDecimal price, int stock, ProductStatus status) {
        ProductSummary current = getProduct(productId);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("product name required");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("positive price required");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
        ProductSummary next = new ProductSummary(
                current.id(),
                categoryId == null ? current.categoryId() : categoryId,
                name.trim(),
                description,
                price,
                stock,
                status == null ? current.status() : status,
                current.createdAt()
        );
        return repository.save(next);
    }

    public ProductSummary reserveStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        ProductSummary product = getProduct(productId);
        if (product.status() != ProductStatus.ON_SALE) {
            throw new IllegalStateException("product is not on sale");
        }
        if (product.stock() < quantity) {
            throw new IllegalStateException("insufficient stock");
        }
        return repository.save(product.withStock(product.stock() - quantity));
    }

    public ProductSummary getProduct(Long productId) {
        return repository.findProduct(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
    }

    public List<ProductSummary> listProducts() {
        return repository.findAll();
    }
}
