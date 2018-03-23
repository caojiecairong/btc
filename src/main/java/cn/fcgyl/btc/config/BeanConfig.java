package cn.fcgyl.btc.config;

import cn.fcgyl.common.utils.RedisUtil;
import cn.fcgyl.zookeeper.config.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author xma
 */
@Configuration
public class BeanConfig {

    @Primary
    @Bean
    public RedisUtil getRedis(ConfigService configService) {
        String host = configService.getConfig("spring.redis.host");
        String port = configService.getConfig("spring.redis.port");
        String password = configService.getConfig("spring.redis.password");
        if (StringUtils.isNotEmpty(password)) {
            return new RedisUtil(host + ":" + port, password);
        } else if (StringUtils.isNotEmpty(port)) {
            return new RedisUtil(host + ":" + port);
        } else {
            return new RedisUtil(host);
        }
    }
}
