package fun.bookish.xmqtt.mqtt.manager;

import fun.bookish.xmqtt.config.AppConfigManager;
import fun.bookish.xmqtt.mqtt.util.TopicNameParser;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * mqtt客户端管理器
 * @author liuxindong
 */
public class MqttClientManager {

    /**
     * 客户端连接集合
     */
    private static final Set<MqttEndpoint> CLIENTS = new ConcurrentHashSet<>();

    /**
     * 主题集合
     */
    private static final Map<String, MqttTopic> TOPIC_MAP = new ConcurrentHashMap<>();

    /**
     * 记录客户端连接
     * @param client
     */
    public static void addClient(MqttEndpoint client) {
        CLIENTS.add(client);
    }

    /**
     * 删除客户端连接
     * @param client
     */
    public static void removeClient(MqttEndpoint client) {
        CLIENTS.remove(client);
        TOPIC_MAP.values().forEach(topic -> {
            topic.getProducers().remove(client);
            topic.getConsumers().remove(client);
        });
    }

    /**
     * 添加客户端订阅关联
     * @param mqttEndpoint
     * @param subscribe
     */
    public static void addClientSubscribe(MqttEndpoint mqttEndpoint, MqttSubscribeMessage subscribe) {
        subscribe.topicSubscriptions().forEach(topicSubscription -> {
            String topicName = topicSubscription.topicName();
            Pattern pattern = TopicNameParser.convert(topicName);
            TOPIC_MAP.forEach((key, value) -> {
                Matcher matcher = pattern.matcher(key);
                if(matcher.matches()){
                    value.getConsumers().add(mqttEndpoint);
                }
            });
        });
    }

    /**
     * 移除客户端订阅关联
     * @param mqttEndpoint
     * @param unsubscribe
     */
    public static void removeClientSubscribe(MqttEndpoint mqttEndpoint, MqttUnsubscribeMessage unsubscribe) {
        unsubscribe.topics().forEach(topicName -> {
            Pattern pattern = TopicNameParser.convert(topicName);
            TOPIC_MAP.forEach((key, value) -> {
                Matcher matcher = pattern.matcher(key);
                if(matcher.matches()){
                    value.getConsumers().remove(mqttEndpoint);
                }
            });
        });
    }

    /**
     * 分发客户端发布的消息
     * @param client
     * @param message
     */
    public static void dispenseMessage(MqttEndpoint client, MqttPublishMessage message) {
        String topicName = message.topicName();
        MqttTopic mqttTopic = TOPIC_MAP.get(topicName);
        if(mqttTopic != null){
            mqttTopic.getProducers().add(client);
        }

        TOPIC_MAP.forEach((key, value) -> {
            Pattern pattern = TopicNameParser.convert(topicName);
            Matcher matcher = pattern.matcher(key);
            if(matcher.matches()){
                value.getConsumers().forEach(consumer -> {
                    consumer.publish(topicName, message.payload(), MqttQoS.valueOf(AppConfigManager.getAppConfig().getQosLevel()), false, false);
                });
            }
        });
    }
}
