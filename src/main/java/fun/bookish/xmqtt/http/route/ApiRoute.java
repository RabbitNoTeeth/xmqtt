package fun.bookish.xmqtt.http.route;

import fun.bookish.xmqtt.http.util.JsonUtil;
import fun.bookish.xmqtt.http.util.RoutingContextUtils;
import fun.bookish.xmqtt.mqtt.manager.MqttClientManager;
import fun.bookish.xmqtt.mqtt.manager.MqttTopic;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mqtt.MqttEndpoint;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * api路由
 * @author liuxindong
 */
public class ApiRoute {

    public void mount(Router router){
        router.get("/api/clients").handler(this::getClientList);
        router.get("/api/topics").handler(this::getTopicList);
    }

    /**
     * 获取客户端列表
     * @param context
     */
    private void getClientList(RoutingContext context) {
        JsonObject params = RoutingContextUtils.getRequestParams(context);
        Integer page = Integer.parseInt(params.getString("page"));
        Integer pageSize = Integer.parseInt(params.getString("pageSize"));
        Map<String, MqttEndpoint> clientMap = MqttClientManager.getClientMap();
        List<JsonObject> clientList = clientMap
                                    .keySet()
                                    .stream()
                                    .sorted()
                                    .skip((page - 1) * pageSize)
                                    .limit(pageSize)
                                    .map(i -> {
                                        MqttEndpoint client = clientMap.get(i);
                                        return new JsonObject()
                                                    .put("clientId", client.clientIdentifier())
                                                    .put("remoteAddress", i);
                                    })
                                    .collect(Collectors.toList());
        context.response().write(JsonUtil.getJsonStr(true,"查询成功", clientList));
        context.next();
    }

    /**
     * 获取主题列表
     * @param context
     */
    private void getTopicList(RoutingContext context) {
        JsonObject params = RoutingContextUtils.getRequestParams(context);
        Integer page = Integer.parseInt(params.getString("page"));
        Integer pageSize = Integer.parseInt(params.getString("pageSize"));
        Map<String, MqttTopic> topicMap = MqttClientManager.getTopicMap();
        List<JsonObject> topicList = topicMap
                                        .keySet()
                                        .stream()
                                        .sorted()
                                        .skip((page - 1) * pageSize)
                                        .limit(pageSize)
                                        .map(topic -> {
                                            MqttTopic mqttTopic = topicMap.get(topic);
                                            return new JsonObject()
                                                    .put("name", topic)
                                                    .put("messageCount", mqttTopic.getMessageCount().get())
                                                    .put("consumerCount", mqttTopic.getConsumers().size())
                                                    .put("producerCount", mqttTopic.getProducers().size());
                                        })
                                        .collect(Collectors.toList());
        context.response().write(JsonUtil.getJsonStr(true,"查询成功", topicList));
        context.next();
    }

}
