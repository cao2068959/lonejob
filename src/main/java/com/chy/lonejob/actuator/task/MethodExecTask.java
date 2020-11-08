package com.chy.lonejob.actuator.task;

import com.chy.lonejob.core.ReflectInvokeRunnable;


public class MethodExecTask implements Task {

    private String taskName;
    ReflectInvokeRunnable reflectInvokeRunnable;

    public MethodExecTask(String taskName, ReflectInvokeRunnable reflectInvokeRunnable) {
        this.taskName = taskName;
        this.reflectInvokeRunnable = reflectInvokeRunnable;
    }

    @Override
    public void run() {
        reflectInvokeRunnable.run();
    }

    @Override
    public String getTaskName() {
        return taskName;
    }
}
