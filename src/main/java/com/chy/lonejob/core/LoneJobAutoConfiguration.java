package com.chy.lonejob.core;


import com.chy.lonejob.zookeeper.ZkTemplate;
import com.chy.lonejob.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ZookeeperAutoConfiguration.class)
@EnableConfigurationProperties(LoneJobProperties.class)
public class LoneJobAutoConfiguration {

    @Bean
    @ConditionalOnBean({ZkTemplate.class})
    public LoneJobRegisterBeanPostProcessor loneJobScan() {
        return new LoneJobRegisterBeanPostProcessor();
    }

}
