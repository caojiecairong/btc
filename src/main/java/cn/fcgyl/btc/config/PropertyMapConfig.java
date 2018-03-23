package cn.fcgyl.btc.config;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author qianlifeng
 */
public class PropertyMapConfig implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        PropertyMapConfig.loadAllEnvProperties();
    }

    /**
     * Default as in PropertyPlaceholderConfigurer
     */
    private static Map<String, String> propertiesMap = new HashMap<>();

    /**
     * 处理一个配置文件
     *
     * @param props 配置文件信息
     */
    private static void processProperties(Properties props) {
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            try {
                propertiesMap.put(keyStr, props.get(keyStr).toString());
            } catch (Exception e) {
                System.out.println("ERROR >>>>>>>>>>>> processProperties异常:");
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载所有配置项
     */
    private static void loadAllEnvProperties() {
        try {
            Properties mainProperties = loadYamlProperties("application.yml");
            String env = System.getProperty("spring.profiles.active", new String(mainProperties.getProperty("spring.profiles.active").getBytes("ISO-8859-1"), "utf-8"));
            propertiesMap.put("env", env);
            Properties properties = loadYamlProperties("application-" + env + ".yml");
            processProperties(mainProperties);
            //具体env的properties覆盖application.properties中相同key的值
            processProperties(properties);
        } catch (IOException e) {
            System.out.println("ERROR >>>>>>>>>>>> loadAllEnvProperties异常");
            e.printStackTrace();
        }
    }

    private static Properties loadYamlProperties(String path) {
        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            PropertySource<?> applicationYamlPropertySource = loader.load("properties", new ClassPathResource(path), null);
            Map source = ((MapPropertySource) applicationYamlPropertySource).getSource();
            Properties properties = new Properties();
            properties.putAll(source);
            return properties;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 优先取命令行key 命令行不存在则取配置文件
     *
     * @param key
     * @return
     */
    static String getProperty(String key) {
        return System.getProperty(key, propertiesMap.get(key));
    }
}