package com.chy.lonejob.core;

import com.chy.lonejob.actuator.MutexActuator;
import com.chy.lonejob.actuator.task.MethodExecTask;
import com.chy.lonejob.actuator.task.SpringScheduledTask;
import com.chy.lonejob.actuator.task.Task;
import com.chy.lonejob.annotations.LoneComponent;
import com.chy.lonejob.annotations.LoneJob;
import com.chy.lonejob.zookeeper.ZkTemplate;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.annotation.Annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class LoneJobRegisterBeanPostProcessor implements BeanFactoryAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private ZkTemplate zkTemplate;

    @Autowired
    private LoneJobProperties loneJobProperties;

    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    public LoneJobRegisterBeanPostProcessor() {
        this.scheduledTaskRegistrar = new ScheduledTaskRegistrar();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            this.beanFactory = (DefaultListableBeanFactory) beanFactory;
            return;
        }

        if (beanFactory instanceof GenericApplicationContext) {
            this.beanFactory = ((GenericApplicationContext) beanFactory).getDefaultListableBeanFactory();
            return;
        }
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LoneComponent component = bean.getClass().getAnnotation(LoneComponent.class);
        if (component != null) {
            doStart(bean, beanName);
        }
        return bean;
    }


    public void doStart(Object bean, String beanName) {
        findMethodByAnnotated(bean.getClass(), LoneJob.class)
                .stream()
                .forEach(element -> loneJobRegister(element, beanName, bean));
    }

    /**
     * 将解析出来的方法注册进 MutexActuator 里面
     *
     * @param element
     */
    private void loneJobRegister(MethodElement<LoneJob> element, String beanName, Object bean) {
        if (element == null) {
            return;
        }
        MutexActuator mutexActuator = new MutexActuator(zkTemplate, loneJobProperties.getWaitMasterReconnect());
        LoneJob loneJob = element.getAnnotated();
        Method method = element.getMethod();
        //生成 taskName, 优先看有没有在 注解里设置,如果没有就用方法名
        String taskName = beanName + "-" + genName(loneJob, method);
        ReflectInvokeRunnable reflectInvokeRunnable = new ReflectInvokeRunnable(method, bean);
        Task task;
        String cron = loneJob.cron();
        if (cron == null || "".equals(cron)) {
            task = new MethodExecTask(taskName, reflectInvokeRunnable);
        } else {
            task = new SpringScheduledTask(scheduledTaskRegistrar, taskName, cron, reflectInvokeRunnable);
        }
        mutexActuator.runTask(task);
    }

    private String genName(LoneJob loneJob, Method method) {
        String name = loneJob.name();
        if (name != null && !"".equals(name)) {
            return name;
        }
        //用户没有指定,那么直接用方法名
        return method.getName();
    }


    private <T extends Annotation> List<MethodElement<T>> findMethodByAnnotated(Class<?> beanClass, Class<T> annotatedClass) {
        List<MethodElement<T>> result = new ArrayList<>();
        Method[] allMethod = beanClass.getDeclaredMethods();
        if (allMethod == null) {
            return result;
        }

        for (Method method : allMethod) {
            T annotation = method.getAnnotation(annotatedClass);
            if (annotation == null) {
                continue;
            }
            MethodElement methodElement = new MethodElement(method, annotation);
            result.add(methodElement);
        }
        return result;
    }


    private TaskScheduler getTaskScheduler() {
        TaskScheduler scheduler = beanFactory.getBean(TaskScheduler.class);
        return scheduler;

    }

    /**
     * 当容器启动完成后去设在 定时任务的线程池
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        scheduledTaskRegistrar.setTaskScheduler(getTaskScheduler());
        scheduledTaskRegistrar.afterPropertiesSet();
    }

    @Data
    static class MethodElement<T> {
        Method method;
        T annotated;

        public MethodElement(Method method, T annotated) {
            this.method = method;
            this.annotated = annotated;
        }
    }


}
