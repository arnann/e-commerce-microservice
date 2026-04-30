package cn.edu.ecommerce.trade.service;

import cn.edu.ecommerce.trade.model.CartItem;
import cn.edu.ecommerce.trade.model.CartView;
import cn.edu.ecommerce.trade.model.OrderItemView;
import cn.edu.ecommerce.trade.model.OrderStatus;
import cn.edu.ecommerce.trade.model.OrderView;
import cn.edu.ecommerce.trade.model.PaymentResult;
import cn.edu.ecommerce.trade.model.PaymentStatus;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class TradeApplicationService {
    private final TradeRepository repository;
    private final ProductCatalogClient productCatalogClient;

    public TradeApplicationService(TradeRepository repository, ProductCatalogClient productCatalogClient) {
        this.repository = repository;
        this.productCatalogClient = productCatalogClient;
    }

    public CartView addToCart(Long userId, Long productId, int quantity) {
        requirePositiveQuantity(quantity);
        ProductSnapshot product = productCatalogClient.getProduct(productId);
        if (product.stock() < quantity) {
            throw new IllegalStateException("insufficient stock");
        }
        List<CartItem> items = repository.addCartItem(
                userId,
                new CartItem(product.productId(), product.name(), product.price(), quantity)
        );
        return toCartView(userId, items);
    }

    public CartView getCart(Long userId) {
        return toCartView(userId, repository.findCart(userId));
    }

    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public OrderView createOrder(Long userId, Map<Long, Integer> productQuantities) {
        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new IllegalArgumentException("order items required");
        }
        List<OrderItemView> items = productQuantities.entrySet().stream()
                .map(entry -> toOrderItem(entry.getKey(), entry.getValue()))
                .toList();
        items.forEach(item -> productCatalogClient.reserveStock(item.productId(), item.quantity()));
        BigDecimal total = items.stream()
                .map(OrderItemView::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        OrderView order = new OrderView(
                repository.nextOrderId(),
                userId,
                items,
                total,
                OrderStatus.PENDING_PAYMENT,
                Instant.now()
        );
        OrderView saved = repository.saveOrder(order);
        repository.clearCart(userId);
        return saved;
    }

    public PaymentResult pay(Long orderId, String channel) {
        OrderView order = repository.findOrder(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
        if (order.status() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("order is not payable");
        }
        OrderView paid = repository.saveOrder(order.withStatus(OrderStatus.PAID));
        String paymentChannel = channel == null || channel.isBlank() ? "MOCK_PAY" : channel;
        return new PaymentResult(paid.id(), paymentChannel, paid.totalAmount(), paid.status(), PaymentStatus.PAID);
    }

    public List<OrderView> listOrders() {
        return repository.findOrders();
    }

    public OrderView ship(Long orderId) {
        OrderView order = repository.findOrder(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
        if (order.status() != OrderStatus.PAID) {
            throw new IllegalStateException("only paid orders can be shipped");
        }
        return repository.saveOrder(order.withStatus(OrderStatus.SHIPPED));
    }

    private OrderItemView toOrderItem(Long productId, int quantity) {
        requirePositiveQuantity(quantity);
        ProductSnapshot product = productCatalogClient.getProduct(productId);
        if (product.stock() < quantity) {
            throw new IllegalStateException("insufficient stock");
        }
        BigDecimal amount = product.price().multiply(BigDecimal.valueOf(quantity));
        return new OrderItemView(product.productId(), product.name(), product.price(), quantity, amount);
    }

    private CartView toCartView(Long userId, List<CartItem> items) {
        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(userId, items, total);
    }

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
