package fun.bookish.xmqtt.mqtt.manager;

import fun.bookish.xmqtt.config.AppConfigManager;
import fun.bookish.xmqtt.mqtt.util.TopicNameParser;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * mqtt客户端管理器
 * @author liuxindong
 */
public class MqttClientManager {

    /**
     * 客户端连接集合
     */
    private static final Map<String, MqttEndpoint> CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 主题集合
     */
    private static final Map<String, MqttTopic> TOPIC_MAP = new ConcurrentHashMap<>();

    /**
     * 记录客户端连接
     * @param client
     */
    public static void addClient(MqttEndpoint client) {
        CLIENT_MAP.put(client.remoteAddress().toString(), client);
    }

    /**
     * 删除客户端连接
     * @param client
     */
    public static void removeClient(MqttEndpoint client) {
        CLIENT_MAP.remove(client.remoteAddress().toString());
        TOPIC_MAP.values().forEach(topic -> {
            topic.getProducers().remove(client);
            topic.getConsumers().remove(client);
        });
    }

    /**
     * 添加客户端订阅关联
     * @param client
     * @param subscribe
     */
    public static void addClientSubscribe(MqttEndpoint client, MqttSubscribeMessage subscribe) {
        subscribe.topicSubscriptions().forEach(topicSubscription -> {
            String topicName = topicSubscription.topicName();
            MqttTopic topic = checkTopicExists(topicName);
            topic.getConsumers().add(client);
            TOPIC_MAP.forEach((key, value) -> {
                Matcher matcher = topic.getPattern().matcher(key);
                if(!key.equals(topicName) && matcher.matches()){
                    value.getConsumers().add(client);
                }
            });
        });
    }

    /**
     * 移除客户端订阅关联
     * @param client
     * @param unsubscribe
     */
    public static void removeClientSubscribe(MqttEndpoint client, MqttUnsubscribeMessage unsubscribe) {
        unsubscribe.topics().forEach(topicName -> {
            MqttTopic topic = checkTopicExists(topicName);
            topic.getConsumers().remove(client);
            TOPIC_MAP.forEach((key, value) -> {
                Matcher matcher = topic.getPattern().matcher(key);
                if(matcher.matches()){
                    value.getConsumers().remove(client);
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
        Set<MqttEndpoint> targetConsumers = new HashSet<>();

        String topicName = message.topicName();
        MqttTopic topic = checkTopicExists(topicName);
        topic.getMessageCount().incrementAndGet();
        topic.getProducers().add(client);

        TOPIC_MAP.forEach((key, value) -> {
            Matcher matcher = value.getPattern().matcher(topicName);
            if(matcher.matches()){
                targetConsumers.addAll(value.getConsumers());
            }
        });

        targetConsumers.forEach(consumer -> {
            if(consumer.isConnected()){
                consumer.publish(topicName, message.payload(), MqttQoS.valueOf(AppConfigManager.getAppConfig().getQosLevel()), false, false);
            }
        });
    }

    /**
     * 检查topic是否存在
     * @param name
     * @return
     */
    private static MqttTopic checkTopicExists(String name){
        MqttTopic result = TOPIC_MAP.get(name);
        if(result == null){
            MqttTopic newOne = new MqttTopic();
            newOne.setName(name);
            newOne.setPattern(TopicNameParser.convert(name));
            newOne.setHasWildCardCharacter(name.contains("+") || name.contains("#"));
            if(TOPIC_MAP.putIfAbsent(name, newOne) == null){
                result = newOne;
            }else{
                result = TOPIC_MAP.get(name);
            }
        }
        return result;
    }

    public static Map<String, MqttEndpoint> getClientMap() {
        return CLIENT_MAP;
    }

    public static Map<String, MqttTopic> getTopicMap() {
        return TOPIC_MAP;
    }
}
