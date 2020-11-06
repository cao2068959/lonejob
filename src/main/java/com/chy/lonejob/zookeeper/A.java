package com.chy.lonejob.zookeeper;


import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;

public class A {

    private static String LOCK_PATH = "/chy/lonejob/lock";


    public static void main(String[] args) throws Exception {

        RetryPolicy retryPolicy = new RetryForever(1000);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)  // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(retryPolicy)
                .namespace("base") // 包含隔离名称
                .build();

        client.start();

        String random = RandomStringUtils.random(8);

        client.create().creatingParentContainersIfNeeded() // 递归创建所需父节点
                .withMode(CreateMode.EPHEMERAL).forPath(LOCK_PATH+"/master","xxxxxx".getBytes());

        CuratorCache curatorCache = CuratorCache.builder(client, LOCK_PATH + "/master").build();
        CuratorCacheListener listener = CuratorCacheListener.builder().forDeletes((c)->{
            System.out.println("-----> 节点发生了变动:"+ c);
        } ).build();

        curatorCache.listenable().addListener(listener);
        curatorCache.start();

        //InterProcessMutex lock = new InterProcessMutex(zkClient, lockPath);

        while (true){
            Thread.sleep(10000);
        }
    }

}
