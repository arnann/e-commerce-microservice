package cn.edu.ecommerce.trade.service;

@FunctionalInterface
public interface ProductCatalogClient {
    ProductSnapshot getProduct(Long productId);

    default ProductSnapshot reserveStock(Long productId, int quantity) {
        return getProduct(productId);
    }
}
