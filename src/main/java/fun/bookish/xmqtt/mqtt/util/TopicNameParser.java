package fun.bookish.xmqtt.mqtt.util;

import java.util.regex.Pattern;

/**
 * 主题名称解析器
 * @author liuxindong
 */
public class TopicNameParser {

    private TopicNameParser(){}

    public static Pattern convert(String name){
        name = name.replaceAll("\\+", "(\\\\w)+");
        name = name.replaceAll("#", "(\\\\w)+([/]\\\\w+)*");
        return Pattern.compile(name);
    }


    public static void main(String[] args) {
        Pattern p1 = convert("xd/AnalogACQ/+/upstream/+");
        Pattern p2 = Pattern.compile("xd/AnalogACQ/(\\w)+/upstream/(\\w)+");
        System.out.println("xxx");
    }
}
