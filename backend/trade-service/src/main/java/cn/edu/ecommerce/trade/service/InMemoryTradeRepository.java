package cn.edu.ecommerce.trade.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.trade.model.CartItem;
import cn.edu.ecommerce.trade.model.OrderView;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTradeRepository {
    private final IdGenerator orderIds = new IdGenerator(9000);
    private final Map<Long, Map<Long, CartItem>> carts = new ConcurrentHashMap<>();
    private final Map<Long, OrderView> orders = new ConcurrentHashMap<>();

    public synchronized List<CartItem> addCartItem(Long userId, CartItem item) {
        Map<Long, CartItem> cart = carts.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
        CartItem existing = cart.get(item.productId());
        CartItem next = existing == null ? item : existing.withQuantity(existing.quantity() + item.quantity());
        cart.put(item.productId(), next);
        return new ArrayList<>(cart.values());
    }

    public List<CartItem> findCart(Long userId) {
        return new ArrayList<>(carts.getOrDefault(userId, Map.of()).values());
    }

    public long nextOrderId() {
        return orderIds.nextId();
    }

    public OrderView saveOrder(OrderView order) {
        orders.put(order.id(), order);
        return order;
    }

    public Optional<OrderView> findOrder(Long orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }
}
