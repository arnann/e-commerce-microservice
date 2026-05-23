package cn.edu.ecommerce.product.controller;

import cn.edu.ecommerce.common.ApiResponse;
import cn.edu.ecommerce.product.model.CreateCategoryRequest;
import cn.edu.ecommerce.product.model.CreateProductRequest;
import cn.edu.ecommerce.product.model.ReserveStockRequest;
import cn.edu.ecommerce.product.model.UpdateProductRequest;
import cn.edu.ecommerce.product.service.ProductCatalogService;
import cn.edu.ecommerce.product.service.ProductImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductCatalogService productCatalogService;
    private final ProductImageStorageService productImageStorageService;

    public ProductController(ProductCatalogService productCatalogService, ProductImageStorageService productImageStorageService) {
        this.productCatalogService = productCatalogService;
        this.productImageStorageService = productImageStorageService;
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.ok("product-service");
    }

    @PostMapping("/categories")
    public ApiResponse<?> createCategory(@RequestBody CreateCategoryRequest request) {
        return ApiResponse.ok(productCatalogService.createCategory(request.name()));
    }

    @GetMapping("/categories")
    public ApiResponse<?> listCategories() {
        return ApiResponse.ok(productCatalogService.listCategories());
    }

    @PostMapping
    public ApiResponse<?> createProduct(@RequestBody CreateProductRequest request) {
        return ApiResponse.ok(productCatalogService.createProduct(
                request.categoryId(),
                request.name(),
                request.description(),
                request.imageUrl(),
                request.price(),
                request.stock()
        ));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<?> publish(@PathVariable Long id) {
        return ApiResponse.ok(productCatalogService.publish(id));
    }

    @PostMapping("/{id}/reserve")
    public ApiResponse<?> reserveStock(@PathVariable Long id, @RequestBody ReserveStockRequest request) {
        return ApiResponse.ok(productCatalogService.reserveStock(id, request.quantity()));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return ApiResponse.ok(productCatalogService.updateProduct(
                id,
                request.categoryId(),
                request.name(),
                request.description(),
                request.imageUrl(),
                request.price(),
                request.stock(),
                request.status()
        ));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProduct(@PathVariable Long id) {
        productCatalogService.deleteProduct(id);
        return ApiResponse.ok("deleted");
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(productImageStorageService.store(file));
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
