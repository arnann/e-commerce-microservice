package cn.edu.ecommerce.product.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.product.model.Category;
import cn.edu.ecommerce.product.model.ProductStatus;
import cn.edu.ecommerce.product.model.ProductSummary;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcProductRepository implements ProductRepository {
    private final JdbcClient jdbcClient;
    private final IdGenerator categoryIds = new IdGenerator(System.currentTimeMillis() / 1000);
    private final IdGenerator productIds = new IdGenerator(System.currentTimeMillis() / 1000 + 1000);

    public JdbcProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Category createCategory(String name) {
        long id = categoryIds.nextId();
        jdbcClient.sql("insert into product_category (id, name, sort_order, status) values (?, ?, ?, 'ENABLED')")
                .params(id, name, (int) id)
                .update();
        return new Category(id, name, Instant.now());
    }

    @Override
    public List<Category> findCategories() {
        return jdbcClient.sql("""
                        select id, name, create_time
                        from product_category
                        where deleted = 0
                        order by sort_order, id
                        """)
                .query((rs, rowNum) -> new Category(
                        rs.getLong("id"),
                        rs.getString("name"),
                        toInstant(rs.getTimestamp("create_time"))
                ))
                .list();
    }

    @Override
    public ProductSummary createProduct(Long categoryId, String name, String description, BigDecimal price, int stock) {
        if (!categoryExists(categoryId)) {
            throw new IllegalArgumentException("category not found");
        }
        long id = productIds.nextId();
        jdbcClient.sql("""
                        insert into product_spu (id, category_id, name, description, price, stock, status)
                        values (?, ?, ?, ?, ?, ?, 'DRAFT')
                        """)
                .params(id, categoryId, name, description, price, stock)
                .update();
        return findProduct(id).orElseThrow();
    }

    @Override
    public Optional<ProductSummary> findProduct(Long id) {
        return jdbcClient.sql("""
                        select id, category_id, name, description, price, stock, status, create_time
                        from product_spu
                        where id = ? and deleted = 0
                        """)
                .param(id)
                .query(this::mapProduct)
                .optional();
    }

    @Override
    public ProductSummary save(ProductSummary product) {
        jdbcClient.sql("""
                        update product_spu
                        set category_id = ?, name = ?, description = ?, price = ?, stock = ?, status = ?
                        where id = ? and deleted = 0
                        """)
                .params(
                        product.categoryId(),
                        product.name(),
                        product.description(),
                        product.price(),
                        product.stock(),
                        product.status().name(),
                        product.id()
                )
                .update();
        return findProduct(product.id()).orElseThrow(() -> new IllegalArgumentException("product not found"));
    }

    @Override
    public List<ProductSummary> findAll() {
        return jdbcClient.sql("""
                        select id, category_id, name, description, price, stock, status, create_time
                        from product_spu
                        where deleted = 0
                        order by id
                        """)
                .query(this::mapProduct)
                .list();
    }

    private boolean categoryExists(Long categoryId) {
        Integer count = jdbcClient.sql("select count(*) from product_category where id = ? and deleted = 0")
                .param(categoryId)
                .query(Integer.class)
                .single();
        return count > 0;
    }

    private ProductSummary mapProduct(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new ProductSummary(
                rs.getLong("id"),
                rs.getLong("category_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getInt("stock"),
                ProductStatus.valueOf(rs.getString("status")),
                toInstant(rs.getTimestamp("create_time"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.now() : timestamp.toInstant();
    }
}
