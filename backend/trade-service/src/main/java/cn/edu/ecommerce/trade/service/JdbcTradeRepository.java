package cn.edu.ecommerce.trade.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.trade.model.CartItem;
import cn.edu.ecommerce.trade.model.OrderItemView;
import cn.edu.ecommerce.trade.model.OrderStatus;
import cn.edu.ecommerce.trade.model.OrderView;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcTradeRepository implements TradeRepository {
    private final JdbcClient jdbcClient;
    private final IdGenerator orderIds = new IdGenerator(System.currentTimeMillis() / 1000 + 9000);

    public JdbcTradeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<CartItem> addCartItem(Long userId, CartItem item) {
        jdbcClient.sql("""
                        insert into cart_item (user_id, product_id, product_name, price, quantity)
                        values (?, ?, ?, ?, ?)
                        on duplicate key update
                            product_name = values(product_name),
                            price = values(price),
                            quantity = case
                                when deleted = 1 then values(quantity)
                                else quantity + values(quantity)
                            end,
                            deleted = 0
                        """)
                .params(userId, item.productId(), item.productName(), item.price(), item.quantity())
                .update();
        return findCart(userId);
    }

    @Override
    public List<CartItem> findCart(Long userId) {
        return jdbcClient.sql("""
                        select product_id, product_name, price, quantity
                        from cart_item
                        where user_id = ? and deleted = 0
                        order by update_time desc, product_id
                        """)
                .param(userId)
                .query((rs, rowNum) -> new CartItem(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity")
                ))
                .list();
    }

    @Override
    public void clearCart(Long userId) {
        jdbcClient.sql("update cart_item set deleted = 1 where user_id = ? and deleted = 0")
                .param(userId)
                .update();
    }

    @Override
    public long nextOrderId() {
        return orderIds.nextId();
    }

    @Override
    @Transactional
    public OrderView saveOrder(OrderView order) {
        int updated = jdbcClient.sql("""
                        update order_info set total_amount = ?, status = ? where id = ? and deleted = 0
                        """)
                .params(order.totalAmount(), order.status().name(), order.id())
                .update();
        if (updated == 0) {
            jdbcClient.sql("""
                            insert into order_info (id, user_id, total_amount, status)
                            values (?, ?, ?, ?)
                            """)
                    .params(order.id(), order.userId(), order.totalAmount(), order.status().name())
                    .update();
            for (OrderItemView item : order.items()) {
                jdbcClient.sql("""
                                insert into order_item (order_id, product_id, product_name, price, quantity, amount)
                                values (?, ?, ?, ?, ?, ?)
                                """)
                        .params(order.id(), item.productId(), item.productName(), item.price(), item.quantity(), item.amount())
                        .update();
            }
        }
        return findOrder(order.id()).orElseThrow();
    }

    @Override
    public Optional<OrderView> findOrder(Long orderId) {
        return jdbcClient.sql("""
                        select id, user_id, total_amount, status, create_time
                        from order_info
                        where id = ? and deleted = 0
                        """)
                .param(orderId)
                .query((rs, rowNum) -> mapOrder(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getBigDecimal("total_amount"),
                        OrderStatus.valueOf(rs.getString("status")),
                        toInstant(rs.getTimestamp("create_time"))
                ))
                .optional();
    }

    @Override
    public List<OrderView> findOrders() {
        return jdbcClient.sql("""
                        select id, user_id, total_amount, status, create_time
                        from order_info
                        where deleted = 0
                        order by create_time desc, id desc
                        """)
                .query((rs, rowNum) -> mapOrder(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getBigDecimal("total_amount"),
                        OrderStatus.valueOf(rs.getString("status")),
                        toInstant(rs.getTimestamp("create_time"))
                ))
                .list();
    }

    private OrderView mapOrder(Long id, Long userId, BigDecimal totalAmount, OrderStatus status, Instant createdAt) {
        List<OrderItemView> items = jdbcClient.sql("""
                        select product_id, product_name, price, quantity, amount
                        from order_item
                        where order_id = ? and deleted = 0
                        order by id
                        """)
                .param(id)
                .query((rs, rowNum) -> new OrderItemView(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("amount")
                ))
                .list();
        return new OrderView(id, userId, items, totalAmount, status, createdAt);
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.now() : timestamp.toInstant();
    }
}
