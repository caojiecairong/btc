package cn.fcgyl.btc.config;

import cn.fcgyl.zookeeper.ZookeeperService;
import cn.fcgyl.zookeeper.config.ConfigService;
import cn.fcgyl.zookeeper.config.IExternalConfig;
import cn.fcgyl.zookeeper.sequence.SequenceService;
import cn.fcgyl.zookeeper.serviceDiscovery.ServiceDiscovery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xma
 */
@Configuration
public class ZookeeperConfig implements IExternalConfig {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    public ZookeeperService zookeeperService() {
        String zookeeperHost = getConfigFromPropertyFile("zookeeper.host");
        if (StringUtils.isEmpty(zookeeperHost)) {
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("===================ERROR===================");
            logger.error("                                           ");
            logger.error("           zookeeper.host 未指定            ");
            logger.error("                                           ");
            logger.error("===================ERROR===================");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            logger.error("                                           ");
            System.exit(-1);
            return null;
        } else {
            return new ZookeeperService(zookeeperHost);
        }
    }

    @Bean
    public ConfigService configService(ZookeeperService zookeeperService) {
        ConfigService configService = new ConfigService(zookeeperService, this);
        return configService;
    }

    @Bean
    public SequenceService sequenceService(ZookeeperService zookeeperService) {
        return new SequenceService(zookeeperService);
    }

    @Bean
    public ServiceDiscovery serviceRegister(ZookeeperService zookeeperService) {
        return new ServiceDiscovery(zookeeperService);
    }

    @Override
    public String getConfigFromPropertyFile(String key) {
        return getConfigFromProperty(key);
    }

    private static String getConfigFromProperty(String key) {
        String value = System.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            value = PropertyMapConfig.getProperty(key);
        }

        return value;
    }
}