package cn.edu.ecommerce.message.model;

public record PublishNoticeRequest(String title, String content, Long publisherId) {
}
