package cn.edu.ecommerce.product;

import cn.edu.ecommerce.product.model.ProductStatus;
import cn.edu.ecommerce.product.model.ProductSummary;
import cn.edu.ecommerce.product.service.InMemoryProductRepository;
import cn.edu.ecommerce.product.service.ProductCatalogService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductCatalogServiceTest {

    @Test
    void publishesProductAndReservesStock() {
        ProductCatalogService service = new ProductCatalogService(new InMemoryProductRepository());
        Long categoryId = service.createCategory("数码配件").id();
        Long productId = service.createProduct(categoryId, "无线耳机", "降噪蓝牙耳机", new BigDecimal("299.00"), 8).id();

        ProductSummary published = service.publish(productId);
        service.reserveStock(productId, 3);

        assertThat(published.status()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(service.getProduct(productId).stock()).isEqualTo(5);
    }

    @Test
    void refusesToReserveMoreThanAvailableStock() {
        ProductCatalogService service = new ProductCatalogService(new InMemoryProductRepository());
        Long categoryId = service.createCategory("图书").id();
        Long productId = service.createProduct(categoryId, "微服务实践", "毕业设计参考书", new BigDecimal("88.00"), 2).id();
        service.publish(productId);

        assertThatThrownBy(() -> service.reserveStock(productId, 3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("insufficient stock");
    }

    @Test
    void updatesImageAndSoftDeletesProduct() {
        ProductCatalogService service = new ProductCatalogService(new InMemoryProductRepository());
        Long categoryId = service.createCategory("家居").id();
        Long productId = service.createProduct(categoryId, "台灯", "护眼台灯", "/api/products/uploads/lamp.png", new BigDecimal("128.00"), 6).id();

        ProductSummary updated = service.updateProduct(productId, categoryId, "台灯 Pro", "护眼台灯升级版", "/api/products/uploads/lamp-pro.png", new BigDecimal("168.00"), 9, ProductStatus.ON_SALE);
        service.deleteProduct(productId);

        assertThat(updated.imageUrl()).isEqualTo("/api/products/uploads/lamp-pro.png");
        assertThat(updated.status()).isEqualTo(ProductStatus.ON_SALE);
        assertThat(service.listProducts()).isEmpty();
        assertThatThrownBy(() -> service.getProduct(productId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("product not found");
    }
}
