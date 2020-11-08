package com.chy.lonejob.zookeeper;


import lombok.Getter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.function.Consumer;

@Getter
public class PathChildrenCacheListenerWrapper implements PathChildrenCacheListener {


    private String id;
    private PathChildrenCacheEvent.Type listenerType;
    private Consumer<ChildData> consumer;

    public PathChildrenCacheListenerWrapper(String id, PathChildrenCacheEvent.Type listenerType, Consumer<ChildData> consumer) {
        this.id = id;
        this.listenerType = listenerType;
        this.consumer = consumer;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
        if(listenerType == type){
            consumer.accept(pathChildrenCacheEvent.getData());
        }

    }
}
