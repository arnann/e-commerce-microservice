package cn.edu.ecommerce.trade.controller;

import cn.edu.ecommerce.common.ApiResponse;
import cn.edu.ecommerce.trade.model.AddCartItemRequest;
import cn.edu.ecommerce.trade.model.CreateOrderRequest;
import cn.edu.ecommerce.trade.model.MockPayRequest;
import cn.edu.ecommerce.trade.service.TradeApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade")
public class TradeController {
    private final TradeApplicationService tradeApplicationService;

    public TradeController(TradeApplicationService tradeApplicationService) {
        this.tradeApplicationService = tradeApplicationService;
    }

    @PostMapping("/cart/items")
    public ApiResponse<?> addToCart(@RequestBody AddCartItemRequest request) {
        return ApiResponse.ok(tradeApplicationService.addToCart(request.userId(), request.productId(), request.quantity()));
    }

    @GetMapping("/cart/{userId}")
    public ApiResponse<?> getCart(@PathVariable Long userId) {
        return ApiResponse.ok(tradeApplicationService.getCart(userId));
    }

    @PostMapping("/orders")
    public ApiResponse<?> createOrder(@RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(tradeApplicationService.createOrder(request.userId(), request.productQuantities()));
    }

    @GetMapping("/orders")
    public ApiResponse<?> listOrders() {
        return ApiResponse.ok(tradeApplicationService.listOrders());
    }

    @PostMapping("/orders/{orderId}/pay")
    public ApiResponse<?> pay(@PathVariable Long orderId, @RequestBody MockPayRequest request) {
        return ApiResponse.ok(tradeApplicationService.pay(orderId, request.channel()));
    }

    @PostMapping("/orders/{orderId}/ship")
    public ApiResponse<?> ship(@PathVariable Long orderId) {
        return ApiResponse.ok(tradeApplicationService.ship(orderId));
    }
}
