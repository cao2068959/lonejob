package com.chy.lonejob.actuator;


import com.chy.lonejob.RandomStringUtils;
import com.chy.lonejob.actuator.task.Task;
import com.chy.lonejob.zookeeper.ZkTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MutexActuator {

    ZkTemplate zkTemplate;
    Task task;
    Integer waitMasterReconnect;
    String id;

    String EXEC_PATH = "/exec";
    String MASTER_PATH = "/master";


    public MutexActuator(ZkTemplate zkTemplate, Integer waitMasterReconnect) {
        this.zkTemplate = zkTemplate;
        this.waitMasterReconnect = waitMasterReconnect;
        id = RandomStringUtils.getRandomString(8);
    }

    public void runTask(Task task) {
        this.task = task;
        doRunTask(true);
    }


    private void doRunTask(boolean isRegister) {

        taskLog("准备执行");
        String masterPath = execPath() + MASTER_PATH;

        //去强占一下 master节点
        boolean master = zkTemplate.addEphemeralNode(masterPath);

        //强占成功,直接执行 task
        if (master) {
            taskLog("获得master节点");
            task.run();
        }

        //下面执行注册监听器的逻辑
        if (!isRegister) {
            return;
        }

        //注册监听来等待 master 下线
        zkTemplate.addDeleteListen(execPath(), "waitMasterDie", childData -> {
            taskLog("master节点 掉线");
            String path = childData.getPath();
            if (!masterPath.equals(path)) {
                return;
            }
            //发现掉线的原来是自己,重新注册上去
            if (master) {
                taskLog("master重连");
                zkTemplate.addEphemeralNode(masterPath);
                return;
            }

            taskLog("等待 master节点 上线");
            //睡一下,可能 master是技术性掉线,马上能重连上来
            try {
                Thread.sleep(waitMasterReconnect);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            doRunTask(false);
        });


    }


    private void taskLog(String context) {
        String tastName = task.getTaskName();
        log.info("id = [" + id + "] ,task [" + tastName + "] : " + context);
    }


    private String execPath() {
        String taskName = task.getTaskName();
        return "/lonejob/" + taskName + EXEC_PATH;
    }
}
