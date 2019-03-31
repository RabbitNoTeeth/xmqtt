package fun.bookish.xmqtt.mqtt.listener;

import fun.bookish.xmqtt.config.AppConfig;
import fun.bookish.xmqtt.config.AppConfigManager;
import fun.bookish.xmqtt.mqtt.manager.MqttClientManager;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * mqtt服务事件监听器
 * @author liuxindong
 */
public class MqttServerEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttServerEventListener.class);

    private MqttServerEventListener(){}

    /**
     * 监听客户端连接成功事件
     * @param client  客户端连接
     */
    public static void onClientConnect(MqttEndpoint client) {
        AppConfig appConfig = AppConfigManager.getAppConfig();
        // 校验登录信息
        MqttAuth auth = client.auth();
        if (auth == null || !appConfig.getUsername().equals(auth.getUsername()) ||
                !appConfig.getPassword().equals(auth.getPassword())){
            LOGGER.warn("客户端连接失败, remoteAddress = {}, clientId = {}, cause = 用户名或密码错误", client.remoteAddress().toString(),
                    client.clientIdentifier());
            client.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return;
        }
        client.accept(false);
        MqttClientManager.addClient(client);
        LOGGER.info("客户端连接成功, remoteAddress = {}, clientId = {}, will = {}", client.remoteAddress().toString(),
                client.clientIdentifier(), client.will() == null ? "null" : client.will().toJson().toString());
    }

    /**
     * 监听客户端的连接断开事件
     * @param client
     */
    public static void onClientDisConnect(MqttEndpoint client) {
        MqttClientManager.removeClient(client);
        LOGGER.info("客户端断开连接, remoteAddress = {}, clientId = {}", client.remoteAddress().toString(), client.clientIdentifier());
    }

    /**
     * 监听客户端的订阅事件
     * @param client
     * @param subscribe
     */
    public static void onClientSubscribe(MqttEndpoint client, MqttSubscribeMessage subscribe) {
        MqttClientManager.addClientSubscribe(client, subscribe);
        LOGGER.info("客户端添加订阅, remoteAddress = {}, clientId = {}, subscribes = {}", client.remoteAddress().toString(),
                client.clientIdentifier(), subscribe.topicSubscriptions().stream().map(MqttTopicSubscription::topicName).collect(Collectors.toList()));
    }

    /**
     * 监听客户端的取消订阅事件
     * @param client
     * @param unsubscribe
     */
    public static void onClientUnSubscribe(MqttEndpoint client, MqttUnsubscribeMessage unsubscribe) {
        MqttClientManager.removeClientSubscribe(client, unsubscribe);
        LOGGER.info("客户端取消订阅, remoteAddress = {}, clientId = {}, unsubscribes = {}", client.remoteAddress().toString(),
                client.clientIdentifier(), unsubscribe.topics());
    }

    /**
     * 监听客户端消息事件
     * @param message
     */
    public static void onClientMessage(MqttEndpoint client, MqttPublishMessage message) {
        if(message.qosLevel() == MqttQoS.AT_LEAST_ONCE){
            // 回复客户端一条PUBACK消息
            client.publishAcknowledge(message.messageId());
        }else if(message.qosLevel() == MqttQoS.EXACTLY_ONCE){
            // 回复客户端一条PUBREC消息
            client.publishReceived(message.messageId());
        }
        MqttClientManager.dispenseMessage(client, message);
        LOGGER.info("客户端发布消息, remoteAddress = {}, clientId = {}, topic = {}, payloadSize = {}bytes", client.remoteAddress().toString(),
                client.clientIdentifier(), message.topicName(), message.payload().length());
    }
}
