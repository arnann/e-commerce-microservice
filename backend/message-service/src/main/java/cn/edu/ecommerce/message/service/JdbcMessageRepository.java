package cn.edu.ecommerce.message.service;

import cn.edu.ecommerce.common.IdGenerator;
import cn.edu.ecommerce.message.model.Notice;
import cn.edu.ecommerce.message.model.UserMessage;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JdbcMessageRepository implements MessageRepository {
    private final JdbcClient jdbcClient;
    private final IdGenerator noticeIds = new IdGenerator(System.currentTimeMillis() / 1000);
    private final IdGenerator messageIds = new IdGenerator(System.currentTimeMillis() / 1000 + 5000);

    public JdbcMessageRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Notice saveNotice(String title, String content, Long publisherId) {
        long id = noticeIds.nextId();
        jdbcClient.sql("""
                        insert into notice (id, title, content, publisher_id, published)
                        values (?, ?, ?, ?, 1)
                        """)
                .params(id, title, content, publisherId)
                .update();
        return findNotice(id).orElseThrow();
    }

    @Override
    public List<Notice> findPublishedNotices() {
        return jdbcClient.sql("""
                        select id, title, content, publisher_id, published, create_time
                        from notice
                        where deleted = 0 and published = 1
                        order by create_time desc, id desc
                        """)
                .query(this::mapNotice)
                .list();
    }

    @Override
    public List<Notice> findAllNotices() {
        return jdbcClient.sql("""
                        select id, title, content, publisher_id, published, create_time
                        from notice
                        where deleted = 0
                        order by create_time desc, id desc
                        """)
                .query(this::mapNotice)
                .list();
    }

    @Override
    public UserMessage saveMessage(Long userId, Long orderId, String content) {
        long id = messageIds.nextId();
        jdbcClient.sql("""
                        insert into user_message (id, user_id, order_id, content, read_flag)
                        values (?, ?, ?, ?, 0)
                        """)
                .params(id, userId, orderId, content)
                .update();
        return findMessage(id).orElseThrow();
    }

    @Override
    public List<UserMessage> findUserMessages(Long userId) {
        return jdbcClient.sql("""
                        select id, user_id, order_id, content, read_flag, create_time
                        from user_message
                        where user_id = ? and deleted = 0
                        order by create_time desc, id desc
                        """)
                .param(userId)
                .query(this::mapUserMessage)
                .list();
    }

    @Override
    public Optional<UserMessage> findMessage(Long messageId) {
        return jdbcClient.sql("""
                        select id, user_id, order_id, content, read_flag, create_time
                        from user_message
                        where id = ? and deleted = 0
                        """)
                .param(messageId)
                .query(this::mapUserMessage)
                .optional();
    }

    @Override
    public void saveMessage(UserMessage message) {
        jdbcClient.sql("update user_message set read_flag = ? where id = ? and deleted = 0")
                .params(message.read() ? 1 : 0, message.id())
                .update();
    }

    private Optional<Notice> findNotice(Long id) {
        return jdbcClient.sql("""
                        select id, title, content, publisher_id, published, create_time
                        from notice
                        where id = ? and deleted = 0
                        """)
                .param(id)
                .query(this::mapNotice)
                .optional();
    }

    private Notice mapNotice(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Notice(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getObject("publisher_id", Long.class),
                rs.getInt("published") == 1,
                toInstant(rs.getTimestamp("create_time"))
        );
    }

    private UserMessage mapUserMessage(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new UserMessage(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getObject("order_id", Long.class),
                rs.getString("content"),
                rs.getInt("read_flag") == 1,
                toInstant(rs.getTimestamp("create_time"))
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.now() : timestamp.toInstant();
    }
}
