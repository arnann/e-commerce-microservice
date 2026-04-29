package cn.edu.ecommerce.message.controller;

import cn.edu.ecommerce.common.ApiResponse;
import cn.edu.ecommerce.message.model.CreateMessageRequest;
import cn.edu.ecommerce.message.model.PublishNoticeRequest;
import cn.edu.ecommerce.message.service.MessageCenterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageCenterService messageCenterService;

    public MessageController(MessageCenterService messageCenterService) {
        this.messageCenterService = messageCenterService;
    }

    @PostMapping("/notices")
    public ApiResponse<?> publishNotice(@RequestBody PublishNoticeRequest request) {
        return ApiResponse.ok(messageCenterService.publishNotice(request.title(), request.content(), request.publisherId()));
    }

    @GetMapping("/notices")
    public ApiResponse<?> listPublishedNotices() {
        return ApiResponse.ok(messageCenterService.listPublishedNotices());
    }

    @PostMapping("/users/{userId}/order-events")
    public ApiResponse<?> createOrderMessage(@PathVariable Long userId, @RequestBody CreateMessageRequest request) {
        return ApiResponse.ok(messageCenterService.createOrderMessage(userId, request.orderId(), request.content()));
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<?> listUserMessages(@PathVariable Long userId) {
        return ApiResponse.ok(messageCenterService.listUserMessages(userId));
    }

    @PostMapping("/users/{userId}/{messageId}/read")
    public ApiResponse<?> markRead(@PathVariable Long userId, @PathVariable Long messageId) {
        messageCenterService.markRead(userId, messageId);
        return ApiResponse.ok(true);
    }
}
