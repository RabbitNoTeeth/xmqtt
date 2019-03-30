package fun.bookish.xmqtt.mqtt.listener;

import fun.bookish.xmqtt.mqtt.manager.MqttClientManager;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.messages.MqttSubscribeMessage;
import io.vertx.mqtt.messages.MqttUnsubscribeMessage;

import java.util.UUID;

/**
 * mqtt服务事件监听器
 * @author liuxindong
 */
public class MqttServerEventListener {

    private MqttServerEventListener(){}

    /**
     * 监听客户端连接成功事件
     * @param client  客户端连接
     */
    public static void onClientConnect(MqttEndpoint client) {
        MqttClientManager.addClient(client);
    }

    /**
     * 监听客户端的连接断开事件
     * @param client
     */
    public static void onClientDisConnect(MqttEndpoint client) {
        MqttClientManager.removeClient(client);
    }

    /**
     * 监听客户端的订阅事件
     * @param mqttEndpoint
     * @param subscribe
     */
    public static void onClientSubscribe(MqttEndpoint mqttEndpoint, MqttSubscribeMessage subscribe) {
        MqttClientManager.addClientSubscribe(mqttEndpoint, subscribe);
    }

    /**
     * 监听客户端的取消订阅事件
     * @param mqttEndpoint
     * @param unsubscribe
     */
    public static void onClientUnSubscribe(MqttEndpoint mqttEndpoint, MqttUnsubscribeMessage unsubscribe) {
        MqttClientManager.removeClientSubscribe(mqttEndpoint, unsubscribe);
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
    }
}
