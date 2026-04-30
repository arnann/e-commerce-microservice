package cn.edu.ecommerce.product.service;

import cn.edu.ecommerce.product.model.Category;
import cn.edu.ecommerce.product.model.ProductSummary;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Category createCategory(String name);

    List<Category> findCategories();

    ProductSummary createProduct(Long categoryId, String name, String description, BigDecimal price, int stock);

    Optional<ProductSummary> findProduct(Long id);

    ProductSummary save(ProductSummary product);

    List<ProductSummary> findAll();
}
