package cn.edu.ecommerce.message.service;

import cn.edu.ecommerce.message.model.Notice;
import cn.edu.ecommerce.message.model.UserMessage;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Notice saveNotice(String title, String content, Long publisherId);

    List<Notice> findPublishedNotices();

    List<Notice> findAllNotices();

    UserMessage saveMessage(Long userId, Long orderId, String content);

    List<UserMessage> findUserMessages(Long userId);

    Optional<UserMessage> findMessage(Long messageId);

    void saveMessage(UserMessage message);
}
