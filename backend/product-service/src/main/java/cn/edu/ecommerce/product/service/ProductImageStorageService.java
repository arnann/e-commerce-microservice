package cn.edu.ecommerce.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadDir;

    public ProductImageStorageService(@Value("${product.image.upload-dir:uploads/product-images}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("image file required");
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("unsupported image type");
        }
        try {
            Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                throw new IllegalStateException("invalid upload path");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/api/products/uploads/" + filename;
        } catch (IOException exception) {
            throw new IllegalStateException("failed to store image", exception);
        }
    }

    private String extractExtension(String filename) {
        String cleaned = StringUtils.hasText(filename) ? filename.trim() : "";
        int dotIndex = cleaned.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleaned.length() - 1) {
            throw new IllegalArgumentException("image extension required");
        }
        return cleaned.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
