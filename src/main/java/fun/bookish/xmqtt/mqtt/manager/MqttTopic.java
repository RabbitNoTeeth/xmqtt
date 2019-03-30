package fun.bookish.xmqtt.mqtt.manager;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.mqtt.MqttEndpoint;
import lombok.Data;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * mqtt主题
 * @author liuxindong
 */
@Data
public class MqttTopic {

    /**
     * 主题名称
     */
    private String name;

    /**
     * 消息数
     */
    private AtomicLong messageCount;

    /**
     * 生产者
     */
    private Set<MqttEndpoint> producers;

    /**
     * 消费者
     */
    private Set<MqttEndpoint> consumers;

    public MqttTopic(){
        this.producers = new ConcurrentHashSet<>();
        this.consumers = new ConcurrentHashSet<>();
        this.messageCount = new AtomicLong(0);
    }

}
