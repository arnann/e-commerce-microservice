package cn.edu.ecommerce.search;

import cn.edu.ecommerce.search.model.ProductDocument;
import cn.edu.ecommerce.search.service.InMemorySearchIndex;
import cn.edu.ecommerce.search.service.SearchRecommendationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRecommendationServiceTest {

    @Test
    void filtersSortsAndRecommendsFromIndexedProducts() {
        SearchRecommendationService service = new SearchRecommendationService(new InMemorySearchIndex());
        service.index(new ProductDocument(1L, 10L, "无线耳机", "蓝牙降噪", new BigDecimal("299.00"), 50, 80));
        service.index(new ProductDocument(2L, 10L, "机械键盘", "热插拔轴体", new BigDecimal("399.00"), 20, 30));
        service.index(new ProductDocument(3L, 11L, "运动水杯", "便携大容量", new BigDecimal("59.00"), 100, 60));

        var results = service.search("无线", 10L, "salesDesc");
        var recommendations = service.recommendForCategory(10L, 1L);

        assertThat(results).extracting(ProductDocument::id).containsExactly(1L);
        assertThat(recommendations).extracting(ProductDocument::id).containsExactly(2L);
    }
}
