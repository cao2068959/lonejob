package com.chy.lonejob.zookeeper;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.listen.StandardListenerManager;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;

import java.util.function.Consumer;

public class ZkTemplate {

    CuratorFramework client;

    public ZkTemplate(CuratorFramework curatorFramework) {
        this.client = curatorFramework;
    }

    /**
     * 添加一个临时节点
     * 添加失败返回 false
     *
     * @param path
     * @param value
     * @return
     */
    public boolean addEphemeralNode(String path, String value) {
        try {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL).forPath(path, value.getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addEphemeralNode(String path) {
        return addEphemeralNode(path, "");
    }

    /**
     * 监听一个 节点的删除事件
     *
     * @param path
     * @param consumer
     */
    public void addDeleteListen(String path, Consumer<ChildData> consumer) {
        CuratorCache curatorCache = CuratorCache.builder(client, path).build();
        CuratorCacheListener listener = CuratorCacheListener.builder().forDeletes(consumer).build();
        StandardListenerManager<CuratorCacheListener> standardListenerManager=
                (StandardListenerManager) curatorCache.listenable();
        standardListenerManager.forEach();

        curatorCache.listenable().addListener(listener);
    }

}
