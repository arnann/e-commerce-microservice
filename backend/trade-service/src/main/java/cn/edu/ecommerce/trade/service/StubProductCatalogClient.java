package cn.edu.ecommerce.trade.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Component;
import org.apache.seata.core.context.RootContext;

import java.util.Map;

@Component
public class StubProductCatalogClient implements ProductCatalogClient {
    private final RestClient restClient;

    public StubProductCatalogClient(@Value("${product.service-uri:${PRODUCT_SERVICE_URI:http://product-service:8102}}") String productServiceUri) {
        this.restClient = RestClient.builder()
                .baseUrl(productServiceUri)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProductSnapshot getProduct(Long productId) {
        Map<String, Object> response = restClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
            throw new IllegalArgumentException("product not found");
        }
        Number id = (Number) data.get("id");
        Number stock = (Number) data.get("stock");
        return new ProductSnapshot(
                id.longValue(),
                String.valueOf(data.get("name")),
                new java.math.BigDecimal(String.valueOf(data.get("price"))),
                stock.intValue()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public ProductSnapshot reserveStock(Long productId, int quantity) {
        RestClient.RequestBodySpec request = restClient.post()
                .uri("/products/{id}/reserve", productId);
        String xid = RootContext.getXID();
        if (xid != null && !xid.isBlank()) {
            request.header(RootContext.KEY_XID, xid);
        }
        Map<String, Object> response = request
                .body(Map.of("quantity", quantity))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (response == null || !(response.get("data") instanceof Map<?, ?> data)) {
            throw new IllegalArgumentException("product not found");
        }
        Number id = (Number) data.get("id");
        Number stock = (Number) data.get("stock");
        return new ProductSnapshot(
                id.longValue(),
                String.valueOf(data.get("name")),
                new java.math.BigDecimal(String.valueOf(data.get("price"))),
                stock.intValue()
        );
    }
}
