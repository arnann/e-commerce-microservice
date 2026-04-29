package cn.edu.ecommerce.message;

import cn.edu.ecommerce.message.model.Notice;
import cn.edu.ecommerce.message.service.InMemoryMessageRepository;
import cn.edu.ecommerce.message.service.MessageCenterService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageCenterServiceTest {

    @Test
    void publishesNoticeAndCreatesUnreadUserMessage() {
        MessageCenterService service = new MessageCenterService(new InMemoryMessageRepository());

        Notice notice = service.publishNotice("五一活动", "全场商品满减", 1L);
        var message = service.createOrderMessage(7L, 9001L, "订单已支付");

        assertThat(service.listPublishedNotices()).extracting(Notice::id).containsExactly(notice.id());
        assertThat(service.listUserMessages(7L)).hasSize(1);
        assertThat(message.read()).isFalse();

        service.markRead(7L, message.id());

        assertThat(service.listUserMessages(7L).get(0).read()).isTrue();
    }
}
