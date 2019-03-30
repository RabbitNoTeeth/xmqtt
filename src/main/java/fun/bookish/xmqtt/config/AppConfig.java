package fun.bookish.xmqtt.config;

import lombok.Data;

/**
 * 应用配置类
 * @author liuxindong
 */
@Data
public class AppConfig {

    /**
     * mqtt服务端口
     */
    private Integer mqttPort;

    /**
     * http服务端口
     */
    private Integer httpPort;

    /**
     * mqtt服务线程数
     */
    private Integer threads;

    /**
     * 连接超时时间
     */
    private Integer timeoutOnConnect;

    /**
     * 推送给客户端消息的qos级别
     */
    private Integer qosLevel;

    /**
     * mqtt连接用户名
     */
    private String username;

    /**
     * mqtt连接密码
     */
    private String password;

}
