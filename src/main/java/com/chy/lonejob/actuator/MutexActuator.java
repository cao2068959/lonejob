package com.chy.lonejob.actuator;


import com.chy.lonejob.actuator.task.Task;
import com.chy.lonejob.zookeeper.ZkTemplate;

public class MutexActuator {

    ZkTemplate zkTemplate;
    Task task;
    String id;
    Integer waitMasterReconnect;

    String EXEC_PATH = "/exec";
    String MASTER_PATH = "/master";


    public MutexActuator(ZkTemplate zkTemplate, Integer waitMasterReconnect) {
        this.zkTemplate = zkTemplate;
        id = "RandomStringUtils.randomAlphabetic(8)";
        this.waitMasterReconnect = waitMasterReconnect;

    }

    public void runTask(Task task) {
        this.task = task;
        doRunTask(true);
    }


    private void doRunTask(boolean isRegister) {
        System.out.println("######## 执行任务开始");

        String masterPath = execPath() + MASTER_PATH;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //去强占一下 master节点
        boolean master = zkTemplate.addEphemeralNode(masterPath);

        //强占成功,直接执行 task
        if (master) {
            task.run();
        }

        //下面执行注册监听器的逻辑
        if (!isRegister) {
            return;
        }

        //注册监听来等待 master 下线
        zkTemplate.addDeleteListen(execPath(), "waitMasterDie", childData -> {
            System.out.println("######## 主节点掉线");
            String path = childData.getPath();
            if (!masterPath.equals(path)) {
                return;
            }
            //发现掉线的原来是自己,重新注册上去
            if(master){
                zkTemplate.addEphemeralNode(masterPath);
                return;
            }

            System.out.println("######## 准备等待主节点上线");
            //睡一下,可能 master是技术性掉线,马上能重连上来
            try {
                Thread.sleep(waitMasterReconnect);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            doRunTask(false);
        });


    }


    private String execPath() {
        String taskName = task.getTaskName();
        return "/" + taskName + EXEC_PATH;
    }
}
