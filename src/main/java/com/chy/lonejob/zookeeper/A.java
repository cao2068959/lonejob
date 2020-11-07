package com.chy.lonejob.zookeeper;


import com.chy.lonejob.actuator.MutexActuator;
import com.chy.lonejob.actuator.task.Task;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryOneTime;

public class A {

    private static String LOCK_PATH = "/chy/lonejob/lock";


    public static void main(String[] args) throws Exception {

        RetryPolicy retryPolicy = new RetryOneTime(1000);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)  // 会话超时时间
                .connectionTimeoutMs(5000) // 连接超时时间
                .retryPolicy(retryPolicy)
                .namespace("base3") // 包含隔离名称
                .build();

        client.getConnectionStateListenable().addListener((a,b)->{
            System.out.println("连接成功" + b.isConnected());
        });
        client.start();


        ZkTemplate zkTemplate = new ZkTemplate(client);
        MutexActuator mutexActuator = new MutexActuator(zkTemplate,1000*5);


        Task task = new Task() {
            @Override
            public void run() {
                System.out.println("-------------> 执行了人物");
            }

            @Override
            public String getTaskName() {
                return "chymethooo12";
            }
        };

        mutexActuator.runTask(task);


        while (true){
            Thread.sleep(10000);
        }
    }

}
