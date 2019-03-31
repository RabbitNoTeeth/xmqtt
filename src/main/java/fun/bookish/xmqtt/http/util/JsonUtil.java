package fun.bookish.xmqtt.http.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * json工具类
 * @author liuxindong
 */
public class JsonUtil {

    private JsonUtil(){}

    private static JsonObject getJson(boolean success, String message, Object data, Long total){
        return new JsonObject().put("success",success).put("message",message).put("data",data).put("total",total);
    }

    public static String getJsonStr(boolean success, String message, Object data){
        return Json.encode(getJson(success, message, data, null));
    }

    public static String getJsonStr(boolean success, String message, Object data, Long total){
        return Json.encode(getJson(success, message, data, total));
    }

}
