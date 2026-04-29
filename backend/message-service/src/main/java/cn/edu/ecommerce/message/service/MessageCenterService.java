package cn.edu.ecommerce.message.service;

import cn.edu.ecommerce.message.model.Notice;
import cn.edu.ecommerce.message.model.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageCenterService {
    private final InMemoryMessageRepository repository;

    public MessageCenterService(InMemoryMessageRepository repository) {
        this.repository = repository;
    }

    public Notice publishNotice(String title, String content, Long publisherId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("notice title required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("notice content required");
        }
        return repository.saveNotice(title.trim(), content.trim(), publisherId);
    }

    public List<Notice> listPublishedNotices() {
        return repository.findPublishedNotices();
    }

    public UserMessage createOrderMessage(Long userId, Long orderId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("message content required");
        }
        return repository.saveMessage(userId, orderId, content.trim());
    }

    public List<UserMessage> listUserMessages(Long userId) {
        return repository.findUserMessages(userId);
    }

    public void markRead(Long userId, Long messageId) {
        UserMessage message = repository.findMessage(messageId)
                .orElseThrow(() -> new IllegalArgumentException("message not found"));
        if (!message.userId().equals(userId)) {
            throw new IllegalArgumentException("message does not belong to user");
        }
        repository.saveMessage(message.markRead());
    }
}
