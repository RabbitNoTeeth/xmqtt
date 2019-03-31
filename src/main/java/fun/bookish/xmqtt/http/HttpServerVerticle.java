package fun.bookish.xmqtt.http;

import fun.bookish.xmqtt.config.AppConfig;
import fun.bookish.xmqtt.config.AppConfigManager;
import fun.bookish.xmqtt.http.handler.*;
import fun.bookish.xmqtt.http.route.ApiRoute;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;


/**
 * http服务部署单元
 * @author liuxindong
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final AppConfig APP_CONFIG = AppConfigManager.getAppConfig();

    @Override
    public void start() throws Exception {

        Router router = Router.router(this.vertx);
        configRouter(router);
        this.vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(APP_CONFIG.getHttpPort());

    }

    private void configRouter(Router router) {

        router.route()
                .failureHandler(new RouteFailureHandler())
                .handler(new CrosHandler())
                .handler(CookieHandler.create())
                .handler(BodyHandler.create())
                .handler(new RequestParamsHandler())
                .handler(new ResponsePreHandler())
                .handler(StaticHandler.create().setIndexPage("index.html").setWebRoot("dist"));

        new ApiRoute().mount(router);

        router.route()
                .handler(new ResponsePostHandler());

    }
}
