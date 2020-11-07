package com.chy.lonejob.zookeeper;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperAutoConfiguration {

    private static String LOCK_PATH = "/chy/lonejob/lock";

    ZookeeperProperties zookeeperProperties;

    public ZookeeperAutoConfiguration(ZookeeperProperties zookeeperProperties) {
        this.zookeeperProperties = zookeeperProperties;
    }

    public CuratorFramework lonejobCuratorFramework() {
        RetryPolicy retryPolicy = new RetryForever(zookeeperProperties.getRetryInterval());
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperProperties.getAddress())
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeout())  // 会话超时时间
                .connectionTimeoutMs(zookeeperProperties.getConnectionTimeout()) // 连接超时时间
                .retryPolicy(retryPolicy)
                .namespace(zookeeperProperties.getNamespace()) // 包含隔离名称
                .build();
        client.start();
        return client;
    }

    @Bean
    public ZkTemplate zkTemplate() {
        CuratorFramework curatorFramework = lonejobCuratorFramework();
        ZkTemplate zkTemplate = new ZkTemplate(curatorFramework);
        return zkTemplate;
    }

}
