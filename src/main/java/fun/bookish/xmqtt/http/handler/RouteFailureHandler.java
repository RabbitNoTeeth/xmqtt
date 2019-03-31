package fun.bookish.xmqtt.http.handler;

import fun.bookish.xmqtt.http.util.RoutingContextUtils;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * route异常处理器
 * @author liuxindong
 */
public class RouteFailureHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteFailureHandler.class);

    @Override
    public void handle(RoutingContext context) {
        LOGGER.error("api = {}, params = {}", context.request().path(), RoutingContextUtils.getRequestParams(context).toString(),
                context.failure());
    }

}
