package cn.edu.ecommerce.trade;

import cn.edu.ecommerce.trade.model.CartView;
import cn.edu.ecommerce.trade.model.OrderStatus;
import cn.edu.ecommerce.trade.model.PaymentStatus;
import cn.edu.ecommerce.trade.service.InMemoryTradeRepository;
import cn.edu.ecommerce.trade.service.ProductSnapshot;
import cn.edu.ecommerce.trade.service.TradeApplicationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TradeApplicationServiceTest {

    @Test
    void addsCartItemCreatesOrderAndCompletesMockPayment() {
        TradeApplicationService service = new TradeApplicationService(
                new InMemoryTradeRepository(),
                productId -> new ProductSnapshot(productId, "无线耳机", new BigDecimal("299.00"), 10)
        );

        CartView cart = service.addToCart(7L, 101L, 2);
        var order = service.createOrder(7L, Map.of(101L, 2));
        var paid = service.pay(order.id(), "MOCK_PAY");

        assertThat(cart.items()).hasSize(1);
        assertThat(order.totalAmount()).isEqualByComparingTo("598.00");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(paid.orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paid.paymentStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
