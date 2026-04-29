package cn.edu.ecommerce.search.controller;

import cn.edu.ecommerce.common.ApiResponse;
import cn.edu.ecommerce.search.model.ProductDocument;
import cn.edu.ecommerce.search.service.SearchRecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {
    private final SearchRecommendationService searchRecommendationService;

    public SearchController(SearchRecommendationService searchRecommendationService) {
        this.searchRecommendationService = searchRecommendationService;
    }

    @PostMapping("/index")
    public ApiResponse<?> index(@RequestBody ProductDocument document) {
        searchRecommendationService.index(document);
        return ApiResponse.ok(document);
    }

    @GetMapping("/products")
    public ApiResponse<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "salesDesc") String sort
    ) {
        return ApiResponse.ok(searchRecommendationService.search(keyword, categoryId, sort));
    }

    @GetMapping("/recommendations")
    public ApiResponse<?> recommendations(@RequestParam Long categoryId, @RequestParam Long excludeProductId) {
        return ApiResponse.ok(searchRecommendationService.recommendForCategory(categoryId, excludeProductId));
    }
}
