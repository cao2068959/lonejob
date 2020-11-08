package com.chy.lonejob.actuator.task;

import com.chy.lonejob.core.ReflectInvokeRunnable;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpringScheduledTask implements Task {

    private ScheduledTaskRegistrar registrar;
    private String taskName;
    private String cron;
    private ReflectInvokeRunnable reflectInvokeRunnable;


    private AtomicBoolean isExec = new AtomicBoolean(false);

    public SpringScheduledTask(ScheduledTaskRegistrar registrar, String taskName, String cron, ReflectInvokeRunnable reflectInvokeRunnable) {
        this.registrar = registrar;
        this.taskName = taskName;
        this.cron = cron;
        this.reflectInvokeRunnable = reflectInvokeRunnable;
    }

    @Override
    public void run() {
        if (isExec.get()) {
            return;
        }
        TimeZone timeZone = TimeZone.getDefault();
        this.registrar.scheduleCronTask(new CronTask(reflectInvokeRunnable, new CronTrigger(cron, timeZone)));
        isExec.set(true);
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

}
