package com.chy.lonejob.zookeeper;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;

import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ZkTemplate {

    CuratorFramework client;

    ConcurrentHashMap<String, PathChildrenCache> pathChildrenCacheHolder = new ConcurrentHashMap<>();

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
            String s = client.create().creatingParentContainersIfNeeded()
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
    public void addDeleteListen(String path, String id, Consumer<ChildData> consumer) {
        String deleteId = "delete-" + id;

        PathChildrenCache pathChildrenCache = getPathChildren(path);


        AtomicBoolean isRegister = new AtomicBoolean(false);
        pathChildrenCache.getListenable().forEach((listener) -> {
            if (listener instanceof PathChildrenCacheListenerWrapper) {
                return null;
            }
            PathChildrenCacheListenerWrapper wrapper = (PathChildrenCacheListenerWrapper) listener;
            if (deleteId.equals(wrapper.getId())) {
                isRegister.set(true);
            }
            return null;
        });
        //放置重复注册
        if (isRegister.get()) {
            return;
        }
        PathChildrenCacheListenerWrapper listenerWrapper = new PathChildrenCacheListenerWrapper(id, PathChildrenCacheEvent.Type.CHILD_REMOVED, consumer);
        pathChildrenCache.getListenable().addListener(listenerWrapper);

    }


    private PathChildrenCache getPathChildren(String path) {
        PathChildrenCache pathChildrenCache = pathChildrenCacheHolder.get(path);
        if (pathChildrenCache != null) {
            return pathChildrenCache;
        }

        synchronized (pathChildrenCacheHolder) {
            pathChildrenCache = pathChildrenCacheHolder.get(path);
            if (pathChildrenCache != null) {
                return pathChildrenCache;
            }
            pathChildrenCache = new PathChildrenCache(client, path, true);
            try {
                pathChildrenCache.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            pathChildrenCacheHolder.put(path, pathChildrenCache);
            return pathChildrenCache;
        }
    }


}
