package cn.edu.ecommerce.product.controller;

import cn.edu.ecommerce.common.ApiResponse;
import cn.edu.ecommerce.product.model.CreateCategoryRequest;
import cn.edu.ecommerce.product.model.CreateProductRequest;
import cn.edu.ecommerce.product.service.ProductCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @PostMapping("/categories")
    public ApiResponse<?> createCategory(@RequestBody CreateCategoryRequest request) {
        return ApiResponse.ok(productCatalogService.createCategory(request.name()));
    }

    @PostMapping
    public ApiResponse<?> createProduct(@RequestBody CreateProductRequest request) {
        return ApiResponse.ok(productCatalogService.createProduct(
                request.categoryId(),
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        ));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<?> publish(@PathVariable Long id) {
        return ApiResponse.ok(productCatalogService.publish(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getProduct(@PathVariable Long id) {
        return ApiResponse.ok(productCatalogService.getProduct(id));
    }

    @GetMapping
    public ApiResponse<?> listProducts() {
        return ApiResponse.ok(productCatalogService.listProducts());
    }
}
