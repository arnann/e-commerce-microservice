package cn.edu.ecommerce.product.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ProductStaticResourceConfig implements WebMvcConfigurer {
    private final String uploadDir;

    public ProductStaticResourceConfig(@Value("${product.image.upload-dir:uploads/product-images}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/products/uploads/**")
                .addResourceLocations(location);
    }
}
