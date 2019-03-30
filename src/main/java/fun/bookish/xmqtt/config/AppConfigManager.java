package fun.bookish.xmqtt.config;

/**
 * 应用配置管理器
 * @author liuxindong
 */
public class AppConfigManager {

    private AppConfigManager(){}

    private static AppConfig appConfig = null;

    public static AppConfig getAppConfig() {
        return appConfig;
    }

    public static void setAppConfig(AppConfig appConfig) {
        AppConfigManager.appConfig = appConfig;
    }
}
