package fun.bookish.xmqtt.http.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class JsonUtil {

    private JsonUtil(){}

    private static JsonObject getJson(boolean success, String message, Object data){
        return new JsonObject().put("success",success).put("message",message).put("data",data);
    }

    public static String getJsonStr(boolean success, String message, Object data){
        return Json.encode(getJson(success, message, data));
    }

}
