package fun.bookish.xmqtt.mqtt;

import fun.bookish.xmqtt.config.AppConfig;
import fun.bookish.xmqtt.config.AppConfigManager;
import fun.bookish.xmqtt.mqtt.listener.MqttServerEventListener;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.vertx.core.AbstractVerticle;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;

/**
 * mqtt服务部署单元
 * @author LIUXINDONG
 */
public class MqttServerVerticle extends AbstractVerticle {

    private static final AppConfig APP_CONFIG = AppConfigManager.getAppConfig();

    @Override
    public void start() throws Exception {
        MqttServerOptions options = new MqttServerOptions()
                                            .setPort(APP_CONFIG.getMqttPort())
                                            .setTimeoutOnConnect(APP_CONFIG.getTimeoutOnConnect());
        MqttServer mqttServer = MqttServer.create(vertx, options);
        mqttServer.endpointHandler(this::handleEndpoint).listen();

    }

    private void handleEndpoint(MqttEndpoint mqttEndpoint) {

        // 校验登录信息
        MqttAuth auth = mqttEndpoint.auth();
        if (auth == null || !APP_CONFIG.getUsername().equals(auth.getUsername()) ||
                !APP_CONFIG.getPassword().equals(auth.getPassword())){
            mqttEndpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
            return;
        }

        // 触发监听器的客户端连接事件
        MqttServerEventListener.onClientConnect(mqttEndpoint);

        // 客户端断开连接处理器
        mqttEndpoint.disconnectHandler(v -> {
            // 触发监听器的客户端断开事件
            MqttServerEventListener.onClientDisConnect(mqttEndpoint);
        });

        // 客户端订阅处理器
        mqttEndpoint.subscribeHandler(subscribe -> {
            // 触发监听器的客户端订阅事件
            MqttServerEventListener.onClientSubscribe(mqttEndpoint, subscribe);
        });

        // 客户端取消订阅处理器
        mqttEndpoint.unsubscribeHandler(unsubscribe -> {
            // 触发监听器的客户端取消订阅事件
            MqttServerEventListener.onClientUnSubscribe(mqttEndpoint, unsubscribe);
        });

        // 客户端发布消息处理器
        mqttEndpoint
                .publishHandler(message -> {
                    // 触发监听器的客户端消息事件
                    MqttServerEventListener.onClientMessage(mqttEndpoint, message);
                })
                .publishReleaseHandler(mqttEndpoint::publishComplete);

        // 消息推送处理器
        mqttEndpoint
                .publishAcknowledgeHandler(messageId -> {})
                .publishReceivedHandler(mqttEndpoint::publishRelease)
                .publishCompletionHandler(messageId -> {});

    }

}
