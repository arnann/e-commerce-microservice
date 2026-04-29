package cn.edu.ecommerce.trade.service;

@FunctionalInterface
public interface ProductCatalogClient {
    ProductSnapshot getProduct(Long productId);
}
