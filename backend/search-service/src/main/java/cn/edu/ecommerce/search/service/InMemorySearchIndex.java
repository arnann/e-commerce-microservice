package cn.edu.ecommerce.search.service;

import cn.edu.ecommerce.search.model.ProductDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySearchIndex {
    private final Map<Long, ProductDocument> documents = new ConcurrentHashMap<>();

    public void save(ProductDocument document) {
        documents.put(document.id(), document);
    }

    public List<ProductDocument> findAll() {
        return List.copyOf(documents.values());
    }
}
