package fun.bookish.xmqtt.http.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 路由上下文工具类
 * @author liuxindong
 */
public class RoutingContextUtils {

    private RoutingContextUtils(){}

    private static final String REQUEST_PARAMS_KEY = "REQUEST_PARAMS";

    public static void setRequestParams(RoutingContext context, JsonObject params){
        context.put(REQUEST_PARAMS_KEY, params);
    }

    public static JsonObject getRequestParams(RoutingContext context){
        return context.get(REQUEST_PARAMS_KEY);
    }

}
