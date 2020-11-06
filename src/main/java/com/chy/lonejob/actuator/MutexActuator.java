package com.chy.lonejob.actuator;


import com.chy.lonejob.actuator.task.Task;
import com.chy.lonejob.zookeeper.ZkTemplate;
import org.apache.commons.lang.RandomStringUtils;

public class MutexActuator {

    ZkTemplate zkTemplate;
    Task task;
    String id;
    String EXEC_PATH = "/exec";
    String MASTER_PATH = "/master";
    Integer waitMasterReconnect;


    public MutexActuator(ZkTemplate zkTemplate, Task task) {
        this.zkTemplate = zkTemplate;
        this.task = task;
        id = RandomStringUtils.randomAlphabetic(8);
    }

    public void start() {
        String masterPath = masterPath();
        //去强占一下 master节点
        boolean master = zkTemplate.addEphemeralNode(masterPath);

        if(master){
            //强占成功,直接执行 task
            task.run();
        }else {
            //强占失败，注册监听来等待 master 下线
            zkTemplate.addDeleteListen(masterPath, childData -> {


            });
        }



    }

    private String masterPath() {
        String taskName = task.getTaskName();
        return "/" + taskName + EXEC_PATH + MASTER_PATH;
    }


}
