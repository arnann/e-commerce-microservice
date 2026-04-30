package cn.edu.ecommerce.trade.service;

import cn.edu.ecommerce.trade.model.CartItem;
import cn.edu.ecommerce.trade.model.OrderView;

import java.util.List;
import java.util.Optional;

public interface TradeRepository {
    List<CartItem> addCartItem(Long userId, CartItem item);

    List<CartItem> findCart(Long userId);

    void clearCart(Long userId);

    long nextOrderId();

    OrderView saveOrder(OrderView order);

    Optional<OrderView> findOrder(Long orderId);

    List<OrderView> findOrders();
}
