package com.chy.lonejob.zookeeper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("lonejob.zk")
@Configuration
@Data
public class ZookeeperProperties {

    private String address = "127.0.0.1:2181";
    private String namespace = "chy";
    private int sessionTimeout = 5000;
    private int connectionTimeout = 5000;
    private int retryInterval = 1000;


}
