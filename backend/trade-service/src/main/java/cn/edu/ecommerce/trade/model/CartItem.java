package cn.edu.ecommerce.trade.model;

import java.math.BigDecimal;

public record CartItem(Long productId, String productName, BigDecimal price, int quantity) {

    public CartItem withQuantity(int nextQuantity) {
        return new CartItem(productId, productName, price, nextQuantity);
    }
}
