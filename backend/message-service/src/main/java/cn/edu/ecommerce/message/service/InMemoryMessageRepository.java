package cn.edu.ecommerce.message.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.message.model.Notice;
import cn.edu.ecommerce.message.model.UserMessage;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMessageRepository {
    private final IdGenerator noticeIds = new IdGenerator(1);
    private final IdGenerator messageIds = new IdGenerator(5000);
    private final Map<Long, Notice> notices = new ConcurrentHashMap<>();
    private final Map<Long, UserMessage> messages = new ConcurrentHashMap<>();

    public Notice saveNotice(String title, String content, Long publisherId) {
        Notice notice = new Notice(noticeIds.nextId(), title, content, publisherId, true, Instant.now());
        notices.put(notice.id(), notice);
        return notice;
    }

    public List<Notice> findPublishedNotices() {
        return notices.values().stream()
                .filter(Notice::published)
                .sorted(Comparator.comparing(Notice::id))
                .toList();
    }

    public UserMessage saveMessage(Long userId, Long orderId, String content) {
        UserMessage message = new UserMessage(messageIds.nextId(), userId, orderId, content, false, Instant.now());
        messages.put(message.id(), message);
        return message;
    }

    public List<UserMessage> findUserMessages(Long userId) {
        return messages.values().stream()
                .filter(message -> message.userId().equals(userId))
                .sorted(Comparator.comparing(UserMessage::createdAt).reversed())
                .toList();
    }

    public Optional<UserMessage> findMessage(Long messageId) {
        return Optional.ofNullable(messages.get(messageId));
    }

    public void saveMessage(UserMessage message) {
        messages.put(message.id(), message);
    }
}
