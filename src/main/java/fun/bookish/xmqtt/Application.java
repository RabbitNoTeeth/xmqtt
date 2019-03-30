package fun.bookish.xmqtt;

import fun.bookish.xmqtt.config.AppConfig;
import fun.bookish.xmqtt.config.AppConfigManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 应用启动类
 * @author liuxindong
 */
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // 加载配置文件
        AppConfig appConfig = loadConfig();
        // 部署mqtt服务
        vertx.deployVerticle("fun.bookish.xmqtt.mqtt.MqttServerVerticle", new DeploymentOptions().setInstances(appConfig.getThreads()), res -> {
            if(res.succeeded()){
                LOGGER.info("mqtt服务部署成功, port = {}, threads = {}, timeoutOnConnect = {}", appConfig.getMqttPort(), appConfig.getThreads(), appConfig.getTimeoutOnConnect());
            }else{
                LOGGER.error("mqtt服务部署失败", res.cause());
            }
        });
        // 部署http服务

    }

    /**
     * 加载配置文件
     */
    private static AppConfig loadConfig(){
        // 优先查找当前目录下的配置文件
        InputStream configInputStream;
        try {
            configInputStream = Files.newInputStream(Paths.get("xmqtt-config.yaml"), StandardOpenOption.READ);
        }catch (NoSuchFileException e){
            configInputStream = Application.class.getClassLoader().getResourceAsStream("default-config.yaml");
        }catch (Exception e){
            throw new IllegalStateException(e);
        }
        AppConfig appConfig = new Yaml().loadAs(configInputStream, AppConfig.class);
        AppConfigManager.setAppConfig(appConfig);
        return appConfig;
    }

}
