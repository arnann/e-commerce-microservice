package cn.edu.ecommerce.trade.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StubProductCatalogClient implements ProductCatalogClient {

    @Override
    public ProductSnapshot getProduct(Long productId) {
        return new ProductSnapshot(productId, "示例商品 " + productId, new BigDecimal("99.00"), 100);
    }
}
